#ifndef _EAES_H
#define _EAES_H

#include <random>
#include <openssl/aes.h>
#include <openssl/crypto.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include "include/base64.h"

namespace eaes{
    typedef struct {
        std::unique_ptr<char[]> ciphertext;
        std::unique_ptr<char[]> tag;
    } GCM_ENCRYPT_RESULT;

    class AES {
    public:
        AES(unsigned char* key);
        unsigned char* key; //Key should be 128bit(16byte)
        std::unique_ptr<GCM_ENCRYPT_RESULT> gcm_encrypt(const unsigned char* plaintext,
            int plaintext_len, const unsigned char* iv, int iv_len) const;
        std::unique_ptr<char[]> ecb_encrypt(const unsigned char* plaintext, int plaintext_len) const;
        std::unique_ptr<unsigned char[]> ecb_decrypt(const char* ciphertext_base64) const;
    };

    std::unique_ptr<unsigned char[]> rand_iv(size_t len);
    std::unique_ptr<char[]> rsa_encrypt(const unsigned char* plaintext, int plaintext_len);
}

#endif