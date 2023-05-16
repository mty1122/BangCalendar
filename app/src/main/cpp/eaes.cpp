#include "include/eaes.h"

using namespace eaes;

AES::AES(const unsigned char* key) {
    memcpy(this->key, key, 16);
}

std::unique_ptr<GCM_ENCRYPT_RESULT> AES::gcm_encrypt(const unsigned char* plaintext,
    int plaintext_len, const unsigned char* iv, int iv_len) {
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

std::unique_ptr<unsigned char[]> AES::ecb_decrypt(const char* ciphertext_base64) {
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