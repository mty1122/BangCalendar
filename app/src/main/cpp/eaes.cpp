#include "include/eaes.h"

using namespace eaes;

const char* rsa_key = R"(-----BEGIN PUBLIC KEY-----
MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAx1sydIfTezHGte/XBbze
oDW4s1AdwdUvAGqkCL05CW2sSiT2lmKVE2ywUvU2Hhal1q6BRz90J+1jv5eF5Ub0
M8rH6coeg077sFQmeIevoYEO6cPxgx6dlVjygQs/z23SZK9YqHXnwavNKyhlQTXp
Whj1f+J0uC9670L3BVS7lkbrYenihjNVcoGR/FDWjE8vQ28Fc48IN1cULKooD9W+
kWrcYrDYh9zCiQlXbGnJo116vrurn3enGp8VCtHZixlxypEAYVuN+fLWuF357UHL
IlgGIPqX6rXX3gYRpvuyIwH75+MgABomUP2zbfpxMmvZDjXgrS7nEYh6rzE8y9J6
vwIBAw==
-----END PUBLIC KEY-----
)";

AES::AES(unsigned char* key) {
    this->key = key;
}

std::unique_ptr<GCM_ENCRYPT_RESULT> AES::gcm_encrypt(const unsigned char* plaintext,
    int plaintext_len, const unsigned char* iv, int iv_len) const {
    auto ctx = EVP_CIPHER_CTX_new();
    EVP_EncryptInit_ex(ctx, EVP_aes_128_gcm(), nullptr, nullptr, nullptr);
    EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, iv_len, nullptr);
    EVP_EncryptInit_ex(ctx, nullptr, nullptr, key, iv);
    EVP_CIPHER_CTX_set_padding(ctx, EVP_PADDING_PKCS7);
    unsigned char outbuff[plaintext_len + AES_BLOCK_SIZE];
    int ciphertext_len, outlen;
    EVP_EncryptUpdate(ctx, outbuff, &outlen, plaintext, plaintext_len);
    ciphertext_len = outlen;
    EVP_EncryptFinal_ex(ctx, &outbuff[ciphertext_len], &outlen);
    ciphertext_len += outlen;
    auto ciphertext = base64_encode(outbuff, ciphertext_len);
    EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, 16, outbuff);
    auto tag = base64_encode(outbuff, 16);
    EVP_CIPHER_CTX_free(ctx);
    auto result = std::make_unique<GCM_ENCRYPT_RESULT>();
    result->ciphertext = std::move(ciphertext);
    result->tag = std::move(tag);
    return result;
}

std::unique_ptr<char[]> eaes::AES::ecb_encrypt(const unsigned char* plaintext, int plaintext_len) const {
    AES_KEY aes_key;
    AES_set_encrypt_key(key, 128, &aes_key);
    //Padding
    int remainder = plaintext_len % AES_BLOCK_SIZE;
    int padding = (remainder == 0) ? AES_BLOCK_SIZE : (AES_BLOCK_SIZE - remainder);
    int ciphertext_len = plaintext_len + padding;
    unsigned char inbuff[ciphertext_len], outbuff[ciphertext_len];
    memcpy(inbuff, plaintext, plaintext_len);
    for (int i = plaintext_len; i < ciphertext_len; i++) {
        inbuff[i] = (unsigned char)padding;
    }
    //Encrypt plaintext
    int group, position;
    for (group = 0; group < ciphertext_len / AES_BLOCK_SIZE; group++) {
        position = group * AES_BLOCK_SIZE;
        AES_ecb_encrypt(&inbuff[position], &outbuff[position], &aes_key, AES_ENCRYPT);
    }
    return base64_encode(outbuff, ciphertext_len);
}

std::unique_ptr<unsigned char[]> AES::ecb_decrypt(const char* ciphertext_base64) const {
    int ciphertext_len;
    auto ciphertext = base64_decode(ciphertext_base64, ciphertext_len);
    unsigned char outbuff[ciphertext_len];
    AES_KEY aes_key;
    AES_set_decrypt_key(key, 128, &aes_key);
    //Decrypt ciphertext
    int group, position;
    for (group = 0; group < ciphertext_len / AES_BLOCK_SIZE; group++) {
        position = group * AES_BLOCK_SIZE;
        AES_ecb_encrypt((const unsigned char*)&ciphertext.get()[position], &outbuff[position],
                        &aes_key, AES_DECRYPT);
    }
    //Unpadding
    int plaintext_len = ciphertext_len - (int)outbuff[ciphertext_len - 1];
    std::unique_ptr<unsigned char[]> plaintext(new unsigned char[plaintext_len + 1]);
    memcpy(plaintext.get(), outbuff, plaintext_len);
    plaintext[plaintext_len] = '\0';
    return plaintext;
}

std::unique_ptr<unsigned char[]> eaes::rand_iv(size_t len) {
    static thread_local std::uniform_int_distribution<unsigned> rand(1, 254);
    static thread_local std::mt19937 engine(std::random_device{}());
    std::unique_ptr<unsigned char[]> iv(new unsigned char[len]);
    for (size_t i = 0; i < len; ++i) {
        iv[i] = rand(engine);
    }
    return iv;
}

std::unique_ptr<char[]> eaes::rsa_encrypt(const unsigned char* plaintext, int plaintext_len) {
    BIO *keybio = BIO_new_mem_buf(rsa_key, -1);
    RSA* rsa = RSA_new();
    rsa = PEM_read_bio_RSA_PUBKEY(keybio, &rsa, nullptr, nullptr);
    if (rsa == nullptr) {
        BIO_free_all(keybio);
        return {};
    }

    // RSA_BLOCK_SIZE == Key size(byte) (include padding)
    const int key_size = RSA_size(rsa);
    const int block_size = key_size - 11; //PKCS1_PADDING (exclude padding)
    const int group = plaintext_len / block_size + 1;
    unsigned char outbuff[key_size * group]; //include padding

    int round;
    //Group Encrypt(except last group)
    for (round = 0; round < group - 1; round++) {
        RSA_public_encrypt(block_size, &plaintext[round * block_size],
                           &outbuff[round * key_size], rsa, RSA_PKCS1_PADDING);
    }

    RSA_public_encrypt(plaintext_len % block_size, &plaintext[round * block_size],
                       &outbuff[round * key_size], rsa, RSA_PKCS1_PADDING); //Last group encrypt

    BIO_free_all(keybio);
    RSA_free(rsa);

    return base64_encode(outbuff, key_size * group);
}