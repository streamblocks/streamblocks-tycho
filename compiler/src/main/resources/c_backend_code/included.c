#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <inttypes.h>
#include <signal.h>


static volatile _Bool interrupted = false;


static void handle_SIGINT(int s) {
	interrupted = true;
}


static void run(int argc, char **argv);

int main(int argc, char **argv) {
	if (signal(SIGINT, handle_SIGINT) == SIG_ERR) {
		fputs("ERROR: Could not set signal handler.\n", stderr);
		return EXIT_FAILURE;
	}
	run(argc, argv);
	if (interrupted) {
		fputs("Application interrupted.\n", stderr);
		return EXIT_FAILURE;
	} else {
		return EXIT_SUCCESS;
	}
}


typedef struct {
    size_t head;
    size_t bytes;
    size_t size;
    char *buffer;
} channel_t;


typedef struct {
    size_t channelc;
    channel_t **channelv;
    FILE *stream;
} input_actor_t;


typedef struct {
    channel_t *channel;
    FILE *stream;
} output_actor_t;


static _Bool channel_has_data(channel_t *channel, size_t bytes) {
    return channel->bytes >= bytes;
}


static _Bool channel_has_space(channel_t *channel_vector[], size_t channel_count, size_t bytes) {
    for (size_t i = 0; i < channel_count; i++) {
        if (channel_vector[i]->size - channel_vector[i]->bytes < bytes) {
            return false;
        }
    }
    return true;
}


static void channel_read(channel_t *channel, size_t bytes, void *result) {
    fprintf(stderr, "Read is not implemented");
    exit(1);
}


static void channel_write(channel_t *channel_vector[], size_t channel_count, void *data, size_t bytes) {
	char *dat = data;
    for (size_t c = 0; c < channel_count; c++) {
        channel_t *chan = channel_vector[c];
        for (size_t i = 0; i < bytes; i++) {
            chan->buffer[(chan->head + chan->bytes) % chan->size] = dat[i];
            chan->bytes++;
        }
    }
}


static void channel_peek(channel_t *channel, size_t offset, size_t bytes, void *result) {
	char *res = result;
    for (size_t i = 0; i < bytes; i++) {
        res[i] = channel->buffer[(channel->head+i+offset) % channel->size];
    }
}


static void channel_consume(channel_t *channel, size_t bytes) {
    channel->bytes -= bytes;
    channel->head = (channel->head + bytes) % channel->size;
}


static channel_t *channel_create(size_t size) {
    char *buffer = malloc(size);
    channel_t *channel = malloc(sizeof(channel_t));
    channel->head = 0;
    channel->bytes = 0;
    channel->size = size;
    channel->buffer = buffer;
    return channel;
}


static void channel_destroy(channel_t *channel) {
    free(channel->buffer);
    free(channel);
}


static input_actor_t *input_actor_create(FILE *stream, channel_t *channel_vector[], size_t channel_count) {
    input_actor_t *actor = malloc(sizeof(input_actor_t));
    actor->channelv = channel_vector;
    actor->channelc = channel_count;
    actor->stream = stream;
    return actor;
}


static output_actor_t *output_actor_create(FILE *stream, channel_t *channel) {
    output_actor_t *actor = malloc(sizeof(output_actor_t));
    actor->channel = channel;
    actor->stream = stream;
    return actor;
}


static void input_actor_destroy(input_actor_t *actor) {
    free(actor);
}


static void output_actor_destroy(output_actor_t *actor) {
    free(actor);
}


static _Bool input_actor_run(input_actor_t* actor) {
    size_t bytes = SIZE_MAX;
    for (size_t i = 0; i < actor->channelc; i++) {
        size_t s = actor->channelv[i]->size - actor->channelv[i]->bytes;
        bytes = s < bytes ? s : bytes;
    }
    if (bytes > 0) {
        char buf[bytes];
        bytes = fread(buf, 1, bytes, actor->stream);
        if (bytes > 0) {
            channel_write(actor->channelv, actor->channelc, buf, bytes);
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}


static _Bool output_actor_run(output_actor_t* actor) {
    channel_t *channel = actor->channel;
    if (channel->bytes > 0) {
        size_t wrap_or_end = channel->head + channel->bytes;
        if (wrap_or_end > channel->size) {
            wrap_or_end = channel->size;
        }
        size_t bytes_before_wrap = wrap_or_end - channel->head;
        fwrite(&channel->buffer[channel->head], 1, bytes_before_wrap, actor->stream);

        size_t bytes_after_wrap = channel->bytes - bytes_before_wrap;
        if (bytes_after_wrap > 0) {
            fwrite(&channel->buffer, 1, bytes_after_wrap, actor->stream);
        }

        //channel->head = (channel->head + channel->bytes) % channel->size;
        channel->bytes = 0;
        return true;
    } else {
        return false;
    }
}
