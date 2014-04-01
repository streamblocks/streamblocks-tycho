#ifndef MPEG_CONSTANTS_H
#define MPEG_CONSTANTS_H

typedef int32_t UNKNOWN_TYPE;

static const int32_t NEWVOP = 15;
static const int32_t INTRA = 2;
static const int32_t INTER = 1;
static const int32_t SKIP = 0;
static const int32_t MOTION = 8;

static const int32_t ADDR_SZ = 24;
static const int32_t BTYPE_SZ = 12;
static const int32_t FLAG_SZ = 4;
static const int32_t MB_COORD_SZ = 8;
static const int32_t MV_SZ = 9;
static const int32_t PIX_SZ = 9;
static const int32_t QUANT_SZ = 6;
static const int32_t SAMPLE_SZ = 13;
static const int32_t SAMPLE_COUNT_SZ = 8;
static const int32_t SCALER_SZ       = 7;
static const int32_t FCODE_SHIFT = 6;
static const int32_t FOURMV = 4;
static const int32_t ROUND_TYPE = 32;
static const int32_t VOP_FCODE_FOR_LENGTH = 3;
static const int32_t SEARCHWIN_IN_MB = 3;
static const int32_t MAXW_IN_MB = 120 + 1;
static const int32_t CLOSEST_POW2_MAXW_IN_MB = 128;
static const int32_t MAXH_IN_MB = 68 + 1;
static const int32_t BLOCK_SIZE = 64;
static const int32_t ACCODED = 2;
static const int32_t ACPRED = 1;
static const int32_t DCVAL = 128*8;

#endif

