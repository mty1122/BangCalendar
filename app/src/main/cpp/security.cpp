#include <jni.h>
#include "include/eaes.h"
//#include "android/log.h"

unsigned char signature[16];
eaes::AES* aes;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv *env;
    if (vm->GetEnv((void **)(&env), JNI_VERSION_1_6) != JNI_OK)
        return JNI_ERR;
    //获取security对象
    auto security_class = env->FindClass("com/mty/bangcalendar/util/SecurityUtil");
    //获取签名方法
    auto methodId = env->GetStaticMethodID(security_class, "getSignature", "()[B");
    //获取签名数组
    auto signature_java = (jbyteArray)env->CallStaticObjectMethod(security_class, methodId);
    int jarray_len = env->GetArrayLength(signature_java);
    if (jarray_len > 0) {
        //拷贝签名数组
        env->GetByteArrayRegion(signature_java, 0, jarray_len, (jbyte*)signature);
        //签名校验
        if ((int)signature[0] == 87 || (int)signature[0] == 213) {
            //实例化加密类
            aes = new eaes::AES(signature);
            return JNI_VERSION_1_6;
        }
    }
    return JNI_ERR;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_mty_bangcalendar_util_SecurityUtil_getSmsRequestCode(JNIEnv *env, jobject ) {
    aes->key = signature;
    auto iv = eaes::rand_iv(16);
    auto iv_base64 = eaes::base64_encode(iv.get(), 16);
    auto request_code = aes->gcm_encrypt(signature, 16, iv.get(), 16);
    auto jarr = env->NewObjectArray(3,
        env->FindClass("java/lang/String"), nullptr);
    env->SetObjectArrayElement(jarr, 0, env->NewStringUTF(request_code->ciphertext.get()));
    env->SetObjectArrayElement(jarr, 1, env->NewStringUTF(request_code->tag.get()));
    env->SetObjectArrayElement(jarr, 2, env->NewStringUTF(iv_base64.get()));
    return jarr;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mty_bangcalendar_util_SecurityUtil_getRandomKey(JNIEnv *env, jobject) {
    auto key = eaes::rand_iv(16);
    auto key_base64 = eaes::base64_encode(key.get(), 16);
    return env->NewStringUTF(key_base64.get());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mty_bangcalendar_util_SecurityUtil_encrypt(JNIEnv *env, jobject,
                                                    jstring aes_key, jstring text) {
    auto key_base64 = env->GetStringUTFChars(aes_key, nullptr);
    auto plaintext_len = env->GetStringUTFLength(text); //char字符（字节）数组大小
    auto plaintext_jstring_len = env->GetStringLength(text); //java字符串大小
    char plaintext[plaintext_len + 1];
    env->GetStringUTFRegion(text, 0, plaintext_jstring_len, plaintext);
    int outlen;
    auto key = eaes::base64_decode(key_base64, outlen);
    aes->key = key.get();
    //错误记录：未知原因导致GetStringUTFChars导入字符串失败,改用GetStringUTFRegion后正常
    /*
    __android_log_print(ANDROID_LOG_ERROR, "aes", "%s" ,plaintext);
    __android_log_print(ANDROID_LOG_ERROR, "aes", "%d" ,plaintext_len);
    __android_log_print(ANDROID_LOG_ERROR, "aes", "%s" ,
                        eaes::base64_encode(aes->key, outlen).get());
    */
    auto ciphertext = aes->ecb_encrypt((const unsigned char*)plaintext, plaintext_len);
    env->ReleaseStringUTFChars(aes_key, key_base64);
    return env->NewStringUTF(ciphertext.get());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mty_bangcalendar_util_SecurityUtil_decrypt(JNIEnv *env, jobject,
                                                    jstring aes_key, jstring text) {
    auto key_base64 = env->GetStringUTFChars(aes_key, nullptr);
    auto ciphertext_len = env->GetStringUTFLength(text);
    auto ciphertext_jstring_len = env->GetStringLength(text);
    char ciphertext[ciphertext_len + 1];
    env->GetStringUTFRegion(text, 0, ciphertext_jstring_len, ciphertext);
    int outlen;
    auto key = eaes::base64_decode(key_base64, outlen);
    aes->key = key.get();
    auto plaintext = aes->ecb_decrypt(ciphertext);
    env->ReleaseStringUTFChars(aes_key, key_base64);
    return env->NewStringUTF((char*)plaintext.get());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mty_bangcalendar_util_SecurityUtil_getEncryptedKey(JNIEnv *env, jobject,
                                                            jstring aes_key) {
    auto key_base64 = env->GetStringUTFChars(aes_key, nullptr);
    int outlen;
    auto key = eaes::base64_decode(key_base64, outlen);
    auto encrypted_key = eaes::rsa_encrypt(key.get(), outlen);
    env->ReleaseStringUTFChars(aes_key, key_base64);
    return env->NewStringUTF(encrypted_key.get());
}

extern "C"
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM*, void*) {
    delete aes;
}