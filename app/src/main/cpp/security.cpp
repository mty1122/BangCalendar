#include <jni.h>
#include "include/eaes.h"

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
Java_com_mty_bangcalendar_util_SecurityUtil_getRequestCode(JNIEnv *env, jobject ) {
    auto iv = eaes::rand_iv(16);
    auto iv_base64 = base64_encode(iv.get(), 16);
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
Java_com_mty_bangcalendar_util_SecurityUtil_decrypt(JNIEnv *env, jobject, jstring text) {
    auto ciphertext = env->GetStringUTFChars(text, nullptr);
    auto plaintext = aes->ecb_decrypt(ciphertext);
    env->ReleaseStringUTFChars(text, ciphertext);
    return env->NewStringUTF((char*)plaintext.get());
}

extern "C"
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM*, void*) {
    delete aes;
}

