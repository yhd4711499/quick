/*
 * getpinyin.h
 *
 * Copyright (C) 2006-2007 Li XianJing <http://blog.csdn.net/absurd/>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
 * Boston, MA 02111-1307, USA.
 */

#ifndef GET_PIN_YIN
#define GET_PIN_YIN

#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <PYUtfData.h>
//#define DEBUG

#ifdef DEBUG
#define LOGD(...)__android_log_print(ANDROID_LOG_DEBUG, "getpinyin", __VA_ARGS__)
#else
#define LOGD(...)
#endif

#define UTF8_CHAR_LEN 3
#define SPACE 32
#define NEW_LINE '\0'
#define boolean unsigned short
#define true 1
#define false 0

static unsigned int get_pin_data_length(void);

static boolean char_equal(char a, char b);

static boolean isLetter(char c);

boolean startWith(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap);

int findStartIndex(jint* contentMap, int index);

boolean isIndexInitial(int* contentMap, int index);

boolean looseWith(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int contentMapSize,
		int* hitMap);

void getCapitalArray(
		const char* content,
		const boolean* capitalMap,
		const int* contentMap,
		const int mapSize,
		char* result,
		int* capitalIndexMap);

boolean initialWithEx(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap,
		boolean* capitalMap,
		const int capitalCounts);

int startWithOrContains(const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap);

const char *get_pinyin (const char* hanzhi/*utf-8*/);

int getCapitalMap(boolean* capitalMap, char* chars);

void fillContentMap(int* contentMap,int mapSize, int contentSize);

int get_pinyin_str(const char* chars, char* result, jint* map, int* mapSize, boolean* captitalMap, int* p_capitalCounts);

#endif/*GET_PIN_YIN*/

