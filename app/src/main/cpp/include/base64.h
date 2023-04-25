#ifndef _BASE64_H
#define _BASE64_H

#include<string.h>
#include<openssl/evp.h>
#include <malloc.h>

unsigned char *base64_encode(unsigned char *in);

unsigned char *base64_decode(unsigned char *in);

#endif
