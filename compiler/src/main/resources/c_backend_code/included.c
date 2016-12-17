#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <inttypes.h>
#include <signal.h>
#include <string.h>
#include "fifo.h"
#include "prelude.h"

#pragma gcc diagnostic ignored "-Wparentheses-equality"

static volatile _Bool interrupted = false;


static void handle_SIGINT(int s) {
	interrupted = true;
}

static void run(int argc, char **argv);

int main(int argc, char **argv) {
#ifdef ABORT_ON_SIGINT
	if (signal(SIGINT, handle_SIGINT) == SIG_ERR) {
		fputs("ERROR: Could not set signal handler.\n", stderr);
		return EXIT_FAILURE;
	}
#endif
	run(argc, argv);
	if (interrupted) {
		fputs("Application interrupted.\n", stderr);
		return EXIT_FAILURE;
	} else {
		return EXIT_SUCCESS;
	}
}
