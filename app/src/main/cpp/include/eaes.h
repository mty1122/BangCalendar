#ifndef _SECURITY_H
#define _SECURITY_H

#include <random>
#include <openssl/aes.h>
#include <openssl/crypto.h>
#include "include/base64.h"

namespace eaes{
    typedef struct {
        std::unique_ptr<char[]> ciphertext;
        std::unique_ptr<char[]> tag;
    } GCM_ENCRYPT_RESULT;

    class AES {
    private:
        unsigned char key[16]{};
    public:
        AES(const unsigned char* key);
        std::unique_ptr<GCM_ENCRYPT_RESULT> gcm_encrypt(const unsigned char* plaintext,
            int plaintext_len, const unsigned char* iv, int iv_len);
        std::unique_ptr<unsigned char[]> ecb_decrypt(const char* ciphertext_base64);
    };

    std::unique_ptr<unsigned char[]> rand_iv(size_t len);
}

#endif