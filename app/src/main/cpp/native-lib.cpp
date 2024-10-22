#include <jni.h>
#include <string>
#include <lsplant.hpp>
#include <sys/sysconf.h>
#include <sys/mman.h>
#include "Utils/log.h"
#include "Utils/elf_img.h"
#include <dobby.h>


static size_t page_size_;

// Macros to align addresses to page boundaries
#define ALIGN_DOWN(addr, page_size)         ((addr) & -(page_size))
#define ALIGN_UP(addr, page_size)           (((addr) + ((page_size) - 1)) & ~((page_size) - 1))
static pine::ElfImg elf_img;


static void init(int version);

static void init(int version) {
    elf_img.Init("libart.so", version);
}

static bool Unprotect(void *addr) {
    auto addr_uint = reinterpret_cast<uintptr_t>(addr);
    auto page_aligned_prt = reinterpret_cast<void *>(ALIGN_DOWN(addr_uint, page_size_));
    size_t size = page_size_;
    if (ALIGN_UP(addr_uint + page_size_, page_size_) != ALIGN_UP(addr_uint, page_size_)) {
        size += page_size_;
    }

    int result = mprotect(page_aligned_prt, size, PROT_READ | PROT_WRITE | PROT_EXEC);
    if (result == -1) {
        LOGE("mprotect failed for %p: %s (%d)", addr, strerror(errno), errno);
        return false;
    }
    return true;
}

void *inlineHooker(void *address, void *replacement) {
    if (!Unprotect(address)) {
        return nullptr;
    }

    void *origin_call;
    if (DobbyHook(address, replacement, &origin_call) == RS_SUCCESS) {
        return origin_call;
    } else {
        return nullptr;
    }
}
//void *inlineHooker(void *targetFunc, void *replaceFunc) {
//    auto pageSize = sysconf(_SC_PAGE_SIZE);
//    auto funcAddress = ((uintptr_t) targetFunc) & (-pageSize);
//    mprotect((void *) funcAddress, pageSize, PROT_READ | PROT_WRITE | PROT_EXEC);
//
//    void *originalFunc;
//    if (DobbyHook(targetFunc, replaceFunc, &originalFunc) == RS_SUCCESS) {
//        return originalFunc;
//    }
//    return nullptr;
//}

bool inlineUnHooker(void *originalFunc) {
    return DobbyDestroy(originalFunc) == RT_SUCCESS;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_de_robv_android_xposed_XposedBridge_hook0(JNIEnv *env, jclass clazz, jobject context,
                                               jobject originalMethod, jobject callbackMethod) {
    return lsplant::Hook(env, originalMethod, context, callbackMethod);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_de_robv_android_xposed_XposedBridge_unhook0(JNIEnv *env, jclass clazz, jobject targetMember) {
    return lsplant::UnHook(env, targetMember);
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    page_size_ = static_cast<const size_t>(sysconf(_SC_PAGESIZE));
    {
        char version_str[128];
        if (!__system_property_get("ro.build.version.sdk", version_str)) {
            LOGE("Failed to obtain SDK int");
            return JNI_ERR;
        }
        long version = std::strtol(version_str, nullptr, 10);





        if (version == 0) {
            LOGE("Invalid SDK int %s", version_str);
            return JNI_ERR;
        }
        init(static_cast<int>(version));
    }
    lsplant::InitInfo initInfo{
            .inline_hooker = inlineHooker,
            .inline_unhooker = inlineUnHooker,
            .art_symbol_resolver = [](std::string_view symbol) -> void * {
                return elf_img.GetSymbolAddress(symbol, false, false);
            },
            .art_symbol_prefix_resolver = [](std::string_view symbol) -> void * {
                return elf_img.GetSymbolAddress(symbol, false, true);
            },
    };
    bool result = lsplant::Init(env, initInfo);
    LOGD("lsplant::Init result:%d", result);
    if (!result) {
        LOGE("lsplant init failed");
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}