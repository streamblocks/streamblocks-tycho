#ifndef PRELUDE_H
#define PRELUDE_H

static void print(char *text) {
	fputs(text, stdout);
}

static void println(char *text) {
	puts(text);
}

#endif
