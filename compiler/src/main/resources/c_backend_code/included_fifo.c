    #include <stdlib.h>
    #include <stdio.h>
    #include <stdbool.h>
    #include <inttypes.h>
    #include <signal.h>

  // fifo buffer////
 // #include <e-hal.h>
 // extern e_epiphany_t* g_edev;
  //extern e_mem_t* g_emem;
 // #include <iostream>

 //#endif

 #include <stdint.h>
 /////
extern void* sharedMemory;
extern int core_row, core_col;


void* get_my_global_e_address(void* addr);

void* to_local_address(int col, int row, void* addr);

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


struct fifo_writer;


struct __attribute__((aligned(8))) fifo_reader
{
   int read;
   volatile int write;

	fifo_writer* shadow;
    char  buffer [BUFFER_SIZE+1];
    void init(volatile fifo_table* table);

	void update(){
		shadow->read = read;
	}

};

struct __attribute__((aligned(8))) fifo_writer
{
	volatile int read;
	int write;
	fifo_reader* shadow;
	void init(volatile fifo_table* table);
	void update(){
		shadow->write = write;

	}
};


    typedef struct {
        size_t readerc;
        fifo_reader **readers;
        FILE *stream;
    } input_actor_t;


    typedef struct {
        fifo_reader *reader;
        FILE *stream;
    } output_actor_t;



static _Bool fifo_tokens(fifo_reader* reader, int n)
{
	return (reader->write - reader->read + BUFFER_SIZE + 1) % (BUFFER_SIZE + 1) >= n;
}


bool fifo_space(fifo_writer* writer, int n)
{
	return (BUFFER_SIZE - (writer->write - writer->read + BUFFER_SIZE + 1) % (BUFFER_SIZE + 1)) >= n;
}


void  fifo_consume(fifo_reader* reader, int n)
{
	reader->read = (reader->read + n) % (BUFFER_SIZE + 1);
	reader->update();
}


void fifo_write(fifo_writer* writer, A value)
{
	writer->shadow->buffer[writer->write] = (value);
	writer->write = (writer->write + 1) % (BUFFER_SIZE + 1);
	writer->update();
}


char fifo_peek(fifo_reader* reader, int pos)
{
	return (reader->buffer[(reader->read + pos) % (BUFFER_SIZE + 1)]);
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
            size_t s = BUFFER_SIZE - actor->channelv[i]->bytes;
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
            if (wrap_or_end > BUFFER_SIZE) {
                wrap_or_end = BUFFER_SIZE;
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
