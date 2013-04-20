/**************************************************************************
 * implements by Jeon (poemer@kut.ac.kr) 2012.05.13
 * interface method Android - JNI - Native C
 * YUV420SP Converts to RGB 8888 Format
 * this routines are optimized on ARM based CPU
 ***************************************************************************/
/*android specific headers*/
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

/*standard library*/
#include <time.h>
#include <math.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>

#include "com_androidhuman_example_CameraPreview_ProcessCore.h"
#define LOG_TAG "Native_Proc"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...)if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}
inline int32_t toInt(jbyte pValue) {
	return (0xff & (int32_t) pValue);
}
inline int32_t max(int32_t pValue1, int32_t pValue2) {
	if (pValue1 < pValue2) {
		return pValue2;
	} else {
		return pValue1;
	}
}
inline int32_t clamp(int32_t pValue, int32_t pLowest, int32_t pHighest) {
	if (pValue < 0) {
		return pLowest;
	} else if (pValue > pHighest) {
		return pHighest;
	} else {
		return pValue;
	}
}
inline int32_t color(pColorR, pColorG, pColorB) {
	return 0xFF000000 | ((pColorB << 6) & 0x00FF0000) | ((pColorG >> 2) & 0x0000FF00) | ((pColorR >> 10) & 0x000000FF);
}

JNIEXPORT void JNICALL Java_com_androidhuman_example_CameraPreview_ProcessCore_NativeProc(JNIEnv * pEnv, jobject pObj, jobject pBitmap, jbyteArray pinArray) {
	AndroidBitmapInfo lBitmapInfo;
	uint32_t* lBitmapContent;
	int lRet;
	//	LOGE(1, "**IN JNI bitmap converter IN!");
	//1. retrieve information about the bitmap
	if ((lRet = AndroidBitmap_getInfo(pEnv, pBitmap, &lBitmapInfo)) < 0) {
		LOGE(1, "AndroidBitmap_getInfo failed! error = %d", lRet);
		return;
	}
	if (lBitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE(1, "Bitmap format is not RGBA_8888!");
		return;
	}
	//2. lock the pixel buffer and retrieve a pointer to it
	if ((lRet = AndroidBitmap_lockPixels(pEnv, pBitmap, (void**)&lBitmapContent)) < 0) {
		LOGE(1, "AndroidBitmap_lockPixels() failed! error = %d", lRet);
		return;
	}
	jbyte* lSource = (*pEnv)->GetPrimitiveArrayCritical(pEnv, pinArray, 0);
	if (lSource == NULL) {
		LOGE(1, "Source is null");
		return;
	}
	//LOGE(1, "**Start JNI bitmap converter ");

	int32_t lFrameSize = lBitmapInfo.width * lBitmapInfo.height;
	int32_t lYIndex, lUVIndex;
	int32_t lX, lY;
	int32_t lColorY, lColorU, lColorV;
	int32_t lColorR, lColorG, lColorB;
	int32_t y1192;
	// Processes each pixel and converts YUV to RGB color.
	for (lY = 0, lYIndex = 0; lY < lBitmapInfo.height; ++lY) {
		lColorU = 0; lColorV = 0;
		// Y is divided by 2 because UVs are subsampled vertically.
		// This means that two consecutives iterations refer to the
		// same UV line (e.g when Y=0 and Y=1).
		lUVIndex = lFrameSize + (lY >> 1) * lBitmapInfo.width;

		for (lX = 0; lX < lBitmapInfo.width; ++lX, ++lYIndex) {
			// Retrieves YUV components. UVs are subsampled
			// horizontally too, hence %2 (1 UV for 2 Y).
			lColorY = max(toInt(lSource[lYIndex]) - 16, 0);
			if (!(lX % 2)) {
				lColorV = toInt(lSource[lUVIndex++]) - 128;
				lColorU = toInt(lSource[lUVIndex++]) - 128;
			}
			// Computes R, G and B from Y, U and V.
			y1192 = 1192 * lColorY;

			lColorR = (y1192 + 1634 * lColorV);
			lColorG = (y1192 - 833 * lColorV - 400 * lColorU);
			lColorB = (y1192 + 2066 * lColorU);
			lColorR = clamp(lColorR, 0, 262143);
			lColorG = clamp(lColorG, 0, 262143);
			lColorB = clamp(lColorB, 0, 262143);
			// Combines R, G, B and A into the final pixel color.

			//lBitmapContent[lYIndex] = color(lColorR,lColorG,lColorB);

			if(color(lColorR,lColorG,lColorB)>0xFF7F7F7F){
				lBitmapContent[lYIndex] = 0xFFFFFFFF;
			}
			else{
				lBitmapContent[lYIndex] = 0xFF000000;
			}
		}
	}
	LOGE(1, "**Start JNI bitmap converter %d",lColorR);

	(*pEnv)-> ReleasePrimitiveArrayCritical(pEnv,pinArray,lSource,0);
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
	LOGI(1, "end color conversion2");

}
