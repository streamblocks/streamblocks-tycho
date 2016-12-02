#include <stdint.h>

static int8_t nsGlobal;

void nsSetGlobal(int8_t value) {
	nsGlobal = value;
}

int8_t nsAddGlobal(int8_t value) {
	return nsGlobal + value;
}


static int8_t actorGlobal;

void actorSetGlobal(int8_t value) {
	actorGlobal = value;
}

int8_t actorAddGlobal(int8_t value) {
	return actorGlobal + value;
}
