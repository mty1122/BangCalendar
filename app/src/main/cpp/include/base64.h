#ifndef _BASE64_H
#define _BASE64_H

#include <memory>
#include <cstring>
#include <openssl/evp.h>

namespace eaes {
    std::unique_ptr<unsigned char[]> base64_decode(const char *in, int &outlen);
    std::unique_ptr<char[]> base64_encode(const unsigned char *in, int inlen);
}

#endif