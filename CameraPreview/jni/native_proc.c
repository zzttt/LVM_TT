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

//Java_com_androidhuman_example_CameraPreview_ProcessCore_NativeProc(JNIEnv *pEnv, jobject pObj, jobject pBitmap, jbyteArray pinArray, jint Threshhold);
//JNIEXPORT jint JNICALL Java_com_androidhuman_example_CameraPreview_ProcessCore_NativeProc(JNIEnv * pEnv, jobject pObj, jobject pBitmap, jbyteArray pinArray) {
JNIEXPORT jint JNICALL Java_com_androidhuman_example_CameraPreview_ProcessCore_NativeProc(JNIEnv *pEnv, jobject pObj, jobject pBitmap, jbyteArray pinArray, jint Threshhold) {
	AndroidBitmapInfo lBitmapInfo;
	uint32_t* lBitmapContent;
	uint32_t All_pixelsum = 0;
	uint32_t Area_pixelsum = 0;
	int All_average=0, Area_average=0;

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
	int32_t lYIndex, lUVIndex, lSrcIndex;
	int32_t lX, lY;
	int32_t lColorY, lColorU, lColorV;
	int32_t lColorR, lColorG, lColorB;
	int32_t y1192;
	int32_t Ydata;
	// Processes each pixel and converts YUV to RGB color.
	for (lY = 0, lSrcIndex=0, lYIndex = 291228; lY < lBitmapInfo.height; ++lY) {
		lColorU = 0; lColorV = 0;
		// Y is divided by 2 because UVs are subsampled vertically.
		// This means that two consecutives iterations refer to the
		// same UV line (e.g when Y=0 and Y=1).
		lUVIndex = lFrameSize + (lY >> 1) * lBitmapInfo.width;

		for (lX = 0; lX < lBitmapInfo.width; ++lX, ++lYIndex, ++lSrcIndex) {
			// Retrieves YUV components. UVs are subsampled
			// horizontally too, hence %2 (1 UV for 2 Y).
			lColorY = max(toInt(lSource[lYIndex]) - 16, 0);
			//			if (!(lX % 2)) {
			//				lColorV = toInt(lSource[lUVIndex++]) - 128;
			//				lColorU = toInt(lSource[lUVIndex++]) - 128;
			//			}
			//			// Computes R, G and B from Y, U and V.
			//			y1192 = 1192 * lColorY;
			//
			//			lColorR = (y1192 + 1634 * lColorV);
			//			lColorG = (y1192 - 833 * lColorV - 400 * lColorU);
			//			lColorB = (y1192 + 2066 * lColorU);
			//			lColorR = clamp(lColorR, 0, 262143);
			//			lColorG = clamp(lColorG, 0, 262143);
			//			lColorB = clamp(lColorB, 0, 262143);
			// Combines R, G, B and A into the final pixel color.
			//LOGI(1, "Y = %d | R = %d | G = %d | B = %d |", lColorY,lColorR,lColorG,lColorB);
			//lBitmapContent[lYIndex] = color(lColorR,lColorG,lColorB);

			//lBitmapContent[lYIndex] = lColorY,lColorY,lColor.;
			//			if(color(lColorR,lColorG,lColorB)>0xFF7F7F7F){
			//				lBitmapContent[lYIndex] = 0xFFFFFFFF;
			//			}
			//			else{
			//				lBitmapContent[lYIndex] = 0xFF000000;
			//			}


			//Ydata = 0xFF000000 | (lColorY << 16) | (lColorY << 8)  | (lColorY);
			if(lColorY>Threshhold)
				lBitmapContent[lSrcIndex] = 0xFF000000;
			else
				lBitmapContent[lSrcIndex] = 0xFFFFFFFF;
		}
		lYIndex = lYIndex+824;
		//LOGI(1, "Y = %d | Ydata = %d ", lColorY,Ydata);
	}


	//w=1024, h=768 //// 1024 x ((768/2)-(200/2)) = 290816
	// 290816 + 412 (| 1024/2 -100 = 412)
	// 1024 * 359 (| 768/2 - 25) = 367616 + 412 = 368028
	for (lY = 0, lYIndex = 368028; lY < 30; ++lY) {
		for (lX = 0; lX < lBitmapInfo.width; ++lX, ++lYIndex) {
			lColorY = max(toInt(lSource[lYIndex]) - 16, 0);
			Area_pixelsum += lColorY;
		}
		lYIndex = lYIndex+824; //1024-200
	}

	Area_average = Area_pixelsum/(lBitmapInfo.width*30);

	//return max(toInt(lSource[150]) - 16, 0);
	//return lColorY;
	//LOGE(1, "**Start JNI bitmap converter %d",lColorR);

	(*pEnv)-> ReleasePrimitiveArrayCritical(pEnv,pinArray,lSource,0);
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
	//free(lBitmapContent);
	//free(lSource);
	//LOGI(1, "end color conversion2");

	if(Area_average > Threshhold){
		return 1;
	}
	else{
		return 0;
	}
	//return max(toInt(lSource[19900]) - 16, 0);
}


JNIEXPORT jint JNICALL Java_com_androidhuman_example_CameraPreview_ProcessCore_Gonzalez(JNIEnv *pEnv, jobject pObj, jobject pBitmap, jbyteArray pinArray){
	AndroidBitmapInfo lBitmapInfo;
	uint32_t* lBitmapContent;
	uint32_t all_pixelsum = 0;

	int lRet;

	int tar_his[256]={0};
	int out_his[256]={0};
	int i=0,j=0,his_sum=0;
	int avg=0, threshold=0;
	int low_sum=0, high_sum=0, low_cnt=0, high_cnt=0;

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
	int32_t lYIndex,lSrcIndex;
	int32_t lX, lY;
	int32_t lColorY;

	int tmp_T, T, min_data, max_data;

	//히스토그램
	for (lY = 0, lSrcIndex=0, lYIndex = 291228; lY < lBitmapInfo.height; ++lY) {
		for (lX = 0; lX < lBitmapInfo.width; ++lX, ++lYIndex, ++lSrcIndex) {
			lColorY = max(toInt(lSource[lYIndex]) - 16, 0);
			all_pixelsum += lColorY;
			tar_his[lColorY]++;
		}
		lYIndex = lYIndex+824;
	}

	tmp_T = all_pixelsum/(lBitmapInfo.height*lBitmapInfo.width);

	for(i=0;i<256;i++){
		his_sum += tar_his[i];
		out_his[i]=his_sum;
	}

	do
	{
		avg=tmp_T;
		low_sum=0;
		high_sum=0;
		low_cnt=0;
		high_cnt=0;
		for(i=0; i<256; i++){
			if(i <=tmp_T)
			{
				low_cnt=low_cnt+tar_his[i];
				low_sum = low_sum + tar_his[i]*i;
			}
			else
			{
				high_cnt = high_cnt + tar_his[i];
				high_sum = high_sum + tar_his[i]*i;
			}
		}
		tmp_T = ((low_sum/(double)low_cnt)+(high_sum/(double)high_cnt))/2.0;
	}
	while(tmp_T != avg);
	threshold = (int)tmp_T;

	for(i=0; i<256; i++)
	{
		tar_his[i] = 10000*tar_his[i]/(lBitmapInfo.height*lBitmapInfo.width);
		out_his[i] = 100 * out_his[i]/(lBitmapInfo.height*lBitmapInfo.width);
	}



	//	for(i=0;i<256;i++){
	//		if(hist[i]>0){
	//			min = i;           // 입력영상에서 가장 밝기값이 작은 값
	//			i = 257;
	//		}
	//	}
	//	for(i=255;i>0;i--){
	//		if(hist[i]>0){
	//			max = i;              // 입력영상에서 가장 밝기값이 큰 값
	//			i = -1;
	//		}
	//	}
	//	T = (min+max)/2;


	LOGE(1, "**Start JNI bitmap converter ");

	(*pEnv)-> ReleasePrimitiveArrayCritical(pEnv,pinArray,lSource,0);
	AndroidBitmap_unlockPixels(pEnv, pBitmap);
	//free(lBitmapContent);
	//free(hist);
	return threshold;
	LOGI(1, "end color conversion2");
}
