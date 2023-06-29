#include "include/base64.h"

std::unique_ptr<unsigned char[]> eaes::base64_decode(const char* in, int& outlen) {
    if (in == nullptr)
        return {};

    int inlen = (int)strlen(in);

    if(strstr(in, "==") != nullptr)
        outlen = inlen / 4 * 3 - 2;
    else if(strstr(in, "=") != nullptr)
        outlen = inlen / 4 * 3 -  1;
    else
        outlen = inlen / 4 * 3;

    unsigned char outbuff[outlen];
    std::unique_ptr<unsigned char[]> out(new unsigned char[outlen]);

    EVP_DecodeBlock(outbuff, (const unsigned char*)in, inlen);
    memcpy(out.get(), outbuff, outlen);
    return out;
}

std::unique_ptr<char[]> eaes::base64_encode(const unsigned char* in, int inlen) {
    if (in == nullptr)
        return {};

    int outlen;

    if(inlen % 3 == 0)
        outlen = inlen / 3 * 4;
    else
        outlen = (inlen / 3 + 1) * 4;

    unsigned char outbuff[outlen];
    std::unique_ptr<char[]> out(new char[outlen + 1]);
    out[outlen]='\0';

    EVP_EncodeBlock(outbuff, in, inlen);
    memcpy(out.get(), outbuff, outlen);
    return out;
}