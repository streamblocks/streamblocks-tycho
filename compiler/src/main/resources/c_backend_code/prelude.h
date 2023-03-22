#ifndef PRELUDE_H
#define PRELUDE_H

#include <time.h>
#include <stdlib.h>
#include <math.h>

static void print(char *text) {
	fputs(text, stdout);
}

static void println(char *text) {
	puts(text);
}

static int randInt(int n) {
	static uint8_t initFlag = 1;
	if(initFlag == 1){
		srand(time(NULL));
		initFlag = 0;
	}
    return rand() % n;
}

#endif
