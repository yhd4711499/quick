/*
 * getpinyin.c
 * Copyright (C) 2006-2007 Li XianJing <xianjimli@hotmail.com>
 * Copyright (C) 2006-2007 LiuShen <liushen@topwisesz.com>
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

#include "getpinyin.h"

static boolean char_equal(char a, char b){
	if(a < 58 && a > 49){
		int range = 3, start;
		int no = (a - 48);
		if(no == 9 || no == 7)
			range = 4;
		if(no < 8){
			// from 'A' (ascii = 65)
			start = 65 + (no - 2) * 3;
		}
		else if(no == 8){
			start = 65 + 5 * 3 + 4;
		}else{
			start = 65 + 6 * 3 + 4;
		}

		// convert to capital letter
		if(b > 96 && b < 123)
			b = (char) (b - 32);
		return (unsigned short) (b >= start && b < start + range);
	}else{
		int c = a - b;
		if(c == 0 || c == -32 || c == 32){
			return true;
		}
		return false;
	}
}

static unsigned int get_pin_data_length(void)
{
	return sizeof(py_utf8_offset)/sizeof(py_utf8_offset[0]);
}

boolean isIndexInitial(int* contentMap, int index){
	if(index == 0)
		return true;
	return (unsigned short) (contentMap[index - 1] != contentMap[index]);
}

static boolean isLetter(char c){
	return (unsigned short) ((c > 64 && c < 91) || (c > 96 && c < 123));
}

int findStartIndex(jint* contentMap, int index){
	jint* p_map;
	p_map = contentMap;
	int startIndex = -1;
	while(*p_map != -1){
		startIndex++;
		if(*p_map == index){
			return startIndex;
		}
		p_map++;
	}
	return -1;
}

/**
 * 获得一个扩充的ContentMap，扩充后的ContentMap的大小为contentSize
 * contentMap：【输入，变更】原来的map，将会被更改
 * mapSize：【输入】原来map的大小
 * contentSize：【输入】扩充后大小
 */
void fillContentMap(int* contentMap,int mapSize, int contentSize){
	int result[contentSize + 1];
	int i=0,j = 0,lastNum=-1,lastIndex = -1;;
	for(;i < mapSize; i++){
		if(lastNum == -1){
			lastNum = contentMap[i];
			lastIndex = i;
			continue;
		}
		int curNum = contentMap[i];
		int dif = curNum - lastNum;
		for(;dif > 0; dif--){
			result[j++] = lastIndex;

		}
		lastNum = curNum;
		lastIndex = i;
	}
	lastIndex = i - 1;
	for(;j < contentSize; j++){
		result[j] = lastIndex;
	}
	result[j] = -1;
	for(i = 0; i < contentSize + 1; i++){
		contentMap[i] = result[i];
	}
}

int getCapitalMap(boolean* capitalMap, char* chars){
	boolean* p_capitalMap;
	p_capitalMap = capitalMap;
	int capitalCounts = 0;
	char* p_chars;
	p_chars = chars;
	char c;
	while(*p_chars){
		c = *p_chars;
		if((c > 64 && c < 91) || *(p_chars - 1) == SPACE){
			*p_capitalMap = 1;
			capitalCounts++;
		}else{
			*p_capitalMap = 0;
		}
		p_capitalMap ++;
		p_chars++;
	}
	return capitalCounts;
}

/**
 *
 * content: 【输入】输入字符串
 * capitalMap: 【输入】大写映射
 * contentMap：【输入】原字符串的ContentMap
 * mapSize: 【输入】字符串长度
 * result: 【输出】大写字母列表
 * capitalIndexMap: 【输出】大写字母映射，即将大写字母列表中字符的位置映射为输入字符串中相应字符的位置
 */
void getCapitalArray(
		const char* content,
		const boolean* capitalMap,
		const int* contentMap,
		const int mapSize,
		char* result,
		int* capitalIndexMap){
	int i = 0,j = 0;
	for(;i < mapSize; i++){
		if(capitalMap[i] == 1){
			result[j] = content[i];
			capitalIndexMap[j] = contentMap[i];
			j++;
		}
	}
}

boolean startWith(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap){
	boolean result = false;
	if(size_contents < size_keywords){
		result = 0;
		goto end;
	}
	int i = 0,j = 0;
	int* p_hitMap;
	p_hitMap = hitMap;
	for(;i < size_keywords; i++){
		char c_keyword = keywords[i];
		//if(c_keyword != SPACE){
			for(;j < size_contents; j++){
				char c_content = contents[j];
				if(isLetter(c_content) == false)
					continue;
				else{
					if(char_equal(c_keyword, c_content) != true || j == size_contents -1){
						result = 0;
						goto end;
					}
					else{
						result = 1;
						*(p_hitMap++) = contentMap[j++];
						break;
					}
				}
			}
			if(j == size_contents){
				if(i == size_keywords - 2 || result == 0)
				{
					result = 0;
					goto end;
				}
			}
		//}
	}
end:
	return result;
}

/**
 * 松匹配
 */
boolean looseWith(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int contentMapSize,
		int* hitMap){
	boolean result = 0,continous = -1,startWith = 0,priority = 0;
	if(size_contents < size_keywords){
		result = 0;
		goto end;
	}
	int i = 0,j = 0,contentIndex = 0;
	int* p_hitMap;
	p_hitMap = hitMap;
	//int lastMatchedContentIndexValue = -1;
	boolean startMatch = false;
	for(;i < size_keywords; i++){
		result = 0;
		char c_keyword = keywords[i];
		for(;j < size_contents; j++){
			char c_content = contents[j];
			if(isLetter(c_content) == true){
				if(char_equal(c_keyword, c_content) != true){
					result = 0;
					//if(lastMatchedContentIndexValue != -1 && contentMap[j] != lastMatchedContentIndexValue && startMatch)
					if(startMatch)
						continous = 0;
				}
				else{
					//lastMatchedContentIndexValue = contentMap[j];
					startMatch = true;
					boolean isInitial = isIndexInitial(contentMap, j);
					if((i == 0 && isInitial) || (i!= 0 && (continous || isInitial))){
						if(j == 0)
							startWith = 1;
						result = 1;
						continous = (unsigned short) (continous != 0);
						*(p_hitMap++) = contentMap[j];
						j++;
						break;
					}else{
						result = 0;
						continous = 0;
					}
				}
			}
		}
		if(j == size_contents && result == 0){
				result = 0;
				goto end;
		}
	}
	priority += continous;
	if(continous){
		priority += startWith? 2 : 0;
	}
end:
	return (unsigned short) (result ? result + priority : 0);
}

int startWithOrContains(const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap){
	boolean result = false;
	if(size_contents < size_keywords){
		result = 0;
		goto end;
	}
	int i = 0,j = 0;
	int* p_hitMap;
	p_hitMap = hitMap;
	int remainedSize = size_contents - size_keywords;
	boolean startWith = true;
	for(;i < size_keywords; i++){
		char c_keyword = keywords[i];
			for(;j < size_contents; j++){
				char c_content = contents[j];
				if(isLetter(c_content) == false)
					continue;
				else{
					if(char_equal(c_keyword, c_content) != true || j == size_contents -1){
						result = 0;
						startWith = false;
						p_hitMap = hitMap;
						if(j == size_contents - size_keywords)
							goto end;
					}
					else{
						result = 1;
						*(p_hitMap++) = contentMap[j++];
						break;
					}
				}
			}
			if(j == size_contents){
				if(i == size_keywords - 2 || result == 0)
				{
					result = 0;
					goto end;
				}
			}
		//}
	}
end:
	return result? result + startWith: 0;
}

boolean initialWithEx(
		const char* keywords,
		const int size_keywords,
		const char* contents,
		const int size_contents,
		int* contentMap,
		int* hitMap,
		boolean* capitalMap,
		const int capitalCounts){
	char capitalArray[capitalCounts];
	int capitalIndexMap[capitalCounts];

	getCapitalArray(
			contents,
			capitalMap,
			contentMap,
			size_contents,
			capitalArray,
			capitalIndexMap);

	return looseWith(
			keywords,
			size_keywords,
			capitalArray,
			capitalCounts,
			capitalIndexMap,
			capitalCounts,
			hitMap);
}

const char *get_pinyin (const char* hanzhi)
{
    unsigned int high  = get_pin_data_length() - 1;
    unsigned int low   = 0;
    unsigned int mid   = high/2;
    const char *data   = pinyin_utf8;
	int result = 0;
	
    if (hanzhi == NULL)
        return NULL;

    while (low <= high)
	{
		result = strncmp (hanzhi, data + py_utf8_offset[mid], UTF8_CHAR_LEN);

		if (result == 0)
		{
			return data + py_utf8_offset[mid] + UTF8_CHAR_LEN;
		}
		else if (result < 0)
		{
            if (mid == low)
                break;

			high = mid - 1;
		}
		else
		{
			low = mid + 1;
		}
		mid = (high + low) / 2;
	}

	return NULL;
}


/**
 * 获取字符串的拼音
 * chars：【输入】字符串
 * result：【输出】拼音
 * map：【输入】字符串的ContentMap
 * mapSize：【输入】map的大小
 * captitalMap：【输出】字符串的CapitalMap
 * p_capitalCounts：【输出】CapitalMap的大小
 * return：拼音的长度
 */
int get_pinyin_str(const char* chars, char* result, jint* map, int* mapSize, boolean* captitalMap, int* p_capitalCounts){
	const char* p_chars;
	p_chars = chars;
	char* p_temp_pinyinChars;
	p_temp_pinyinChars = result;
	int index = 0;
	int pastIndex = 0;
	int pastIndexInc = 0;
	char c;

	map[0] = 0;

	while(*p_chars != 0){
		c = *p_chars;
		if(c < 0 || c > 127)	// not in ascii
		{
			// 可以将整个字符串的指针传递给get_pinyin，因为get_pinyin只匹配前UTF8_CHAR_LEN个字符
			const char* pinyin = get_pinyin(p_chars);

			// only add pinyin, ignore any other chars
			if(pinyin){
				const char* p_pinyin;
				p_pinyin = pinyin;

				// 第一个字母大写
				*p_temp_pinyinChars = (char) (*p_pinyin - 32);
				p_temp_pinyinChars++;
				p_pinyin++;

				// 只取第一个发音。
				// 省略\0，否则由此char*生成的jstring将不完整
				while(*p_pinyin != SPACE && *p_pinyin != NEW_LINE){
					*p_temp_pinyinChars = *p_pinyin;
					p_temp_pinyinChars++;
					p_pinyin++;
				}

				// 跳过整个中文字符
				p_chars += UTF8_CHAR_LEN;

				pastIndexInc = (int) (p_pinyin - pinyin);

			}else{
				p_chars++;
				pastIndexInc = 1;
			}
		}else{
			*p_temp_pinyinChars = c;
			p_temp_pinyinChars++;
			p_chars++;
			pastIndexInc = 1;
		}

		if(index != *mapSize -1){
			index++;
			pastIndex += pastIndexInc;
			map[index] = pastIndex;
		}
	}
	*p_temp_pinyinChars = NEW_LINE;
	map[index + 1] = -1;
	*mapSize = index;
	int pinyin_length = (int) (p_temp_pinyinChars - result);

	fillContentMap(map, index, pinyin_length);

	*p_capitalCounts = getCapitalMap(captitalMap, result);
	return pinyin_length;
}

jstring Java_com_ornithopter_quick_plugins_NativeMethods_getPinyin( JNIEnv* env, jobject thiz, jstring jstr, jintArray map)
{
	const char* contents = (*env)->GetStringUTFChars(env,jstr,0);

	// get map
	jint* p_map;
	p_map = (*env)->GetIntArrayElements(env, map, NULL);

	// get pinyin
	char pinyin[256];
	int size = (*env)->GetArrayLength(env, map);
	int* p_size = &size;

	int capitalCounts = 0;
	int* p_capitalCounts = &capitalCounts;

	// capital map
	boolean capitalMap[size];
	int size_contents = get_pinyin_str(contents, pinyin, p_map, p_size, capitalMap, p_capitalCounts);

	(*env)->ReleaseIntArrayElements(env, map, p_map, 0);

	jstring jPinyin = (*env)->NewStringUTF(env, pinyin);

	(*env)->ReleaseStringUTFChars(env, jstr, contents);

	return jPinyin;
}

jstring Java_com_ornithopter_quick_plugins_NativeMethods_removeSpace(JNIEnv* env, jobject thiz, jstring jstr){
	const char* chars = (*env)->GetStringUTFChars(env,jstr,0);
	const char* p_chars;
	p_chars = chars;
	int size = (*env)-> GetStringLength(env, jstr);
	char regex[size];
	int i = 0;
	while(*p_chars){
		if(*p_chars != SPACE)
			regex[i++] = *p_chars;
		p_chars++;
	}
	regex[i] = NEW_LINE;
	jstring jPattern = (*env)->NewStringUTF(env, regex);
	(*env)->ReleaseStringUTFChars(env, jstr, chars);
	return jPattern;
}

jint Java_com_ornithopter_quick_plugins_NativeMethods_isMatch(JNIEnv* env, jobject thiz, jstring keyword, jstring content, jintArray map, jintArray hitMap){
	// get char* of keywords and contents
	const char* keywords = (*env)->GetStringUTFChars(env,keyword,0);
	const char* contents = (*env)->GetStringUTFChars(env,content,0);

	// get map
	jint* p_map;
	p_map = (*env)->GetIntArrayElements(env, map, NULL);

	jint* p_hitMap;
	p_hitMap = (*env)->GetIntArrayElements(env, hitMap, NULL);


	// get pinyin
	char pinyin[256];
	int size = (*env)->GetArrayLength(env, map);
	int* p_size = &size;

	int capitalCounts = 0;
	int* p_capitalCounts = &capitalCounts;

	// capital map
	boolean capitalMap[size];
	int size_contents = get_pinyin_str(contents, pinyin, p_map, p_size, capitalMap, p_capitalCounts);

	// get length
	int size_keywords = (*env)-> GetStringLength(env, keyword);

	boolean result = 0;
	if(size_keywords > 1){
		result = startWith(keywords,size_keywords,pinyin,size_contents, p_map, p_hitMap);
		if(result){
			result += 13;
			LOGD("hit startWith");
			goto end;
		}

		result = initialWithEx(keywords,size_keywords,pinyin,size_contents, p_map, p_hitMap, capitalMap, capitalCounts);
		if(result){
			result += 10;
			LOGD("hit initialWith");
			goto end;
		}

		result = looseWith(keywords,size_keywords,pinyin,size_contents, p_map, size_contents, p_hitMap);
		if(result){
			LOGD("hit looseWith");
			goto end;
		}
	}
	end:
	// release
	(*env)->ReleaseIntArrayElements(env, map, p_map, 0);
	(*env)->ReleaseIntArrayElements(env, hitMap, p_hitMap, 0);
	(*env)->ReleaseStringUTFChars(env, keyword, keywords);
	(*env)->ReleaseStringUTFChars(env, content, contents);
	return result;
}

