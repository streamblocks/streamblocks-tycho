#ifndef MPEG_CONSTANTS_H
#define MPEG_CONSTANTS_H

typedef int32_t UNKNOWN_TYPE;

#define NEWVOP (15)
#define INTRA (2)
#define INTER (1)
#define SKIP (0)
#define MOTION (8)

#define ADDR_SZ (24)
#define BTYPE_SZ (12)
#define FLAG_SZ (4)
#define MB_COORD_SZ (8)
#define MV_SZ (9)
#define PIX_SZ (9)
#define QUANT_SZ (6)
#define SAMPLE_SZ (13)
#define SAMPLE_COUNT_SZ (8)
#define SCALER_SZ (    = 7)
#define FCODE_SHIFT (6)
#define FOURMV (4)
#define ROUND_TYPE (32)
#define VOP_FCODE_FOR_LENGTH (3)
#define SEARCHWIN_IN_MB (3)
#define MAXW_IN_MB (120 + 1)
#define CLOSEST_POW2_MAXW_IN_MB (128)
#define MAXH_IN_MB (68 + 1)
#define BLOCK_SIZE (64)
#define ACCODED (2)
#define ACPRED (1)
#define DCVAL (128*8)

#endif
