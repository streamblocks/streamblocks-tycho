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


struct __attribute__((aligned(8))) reader_container
{
  int8_t row;
  int8_t col;
  void* address;
};
struct __attribute__((aligned(8))) fifo_table
{
  reader_container readers[200];
  void* mem_map[4][4];
};

struct fifo_writer;


struct __attribute__((aligned(8))) fifo_reader
{
    int ID;
    int read;
    volatile int write;

	fifo_writer* shadow;
    char  buffer [BUFFER_SIZE+1];

};

struct __attribute__((aligned(8))) fifo_writer
{
    int ID;
	volatile int read;
	int write;
	fifo_reader* shadow;
};

  	void reader_update(fifo_reader * reader, int read){
   		reader->shadow->read = read;
    }

void reader_init(volatile fifo_table* table,
    fifo_reader * reader, int ID, )
{
    reader->ID = ID;
	reader->read = 0;
	reader->write = 0;
	//std::cout << "rrr " << (uint32_t)this << std::endl;
	get_my_global_e_address(reader);
	table->readers[reader->ID].row = core_row;
	table->readers[reader->ID].col = core_col;

	table->readers[reader->ID].address = get_my_global_e_address(reader);
}

	void writer_init(volatile fifo_table* table,
	    fifo_writer * writer, int ID)
	{
	    writer->ID = ID;
	    writer->read = 0;
	    writer->write = 0;
	    while (table->readers[writer->ID].address == NULL)
		    ;
	    writer->shadow = (fifo_reader*) to_local_address(table->readers[writer->ID].col, table->readers[writer->ID].row, table->readers[writer->ID].address);
	//
	    if (table->readers[writer->ID].col < 0)
	    {
		    writer->shadow->shadow = (fifo_writer*) ((uint32_t)table->mem_map[core_row][core_col] + (uint32_t)writer);
	    }
	    else
	        writer->shadow->shadow = (fifo_writer*) get_my_global_e_address(writer);
    }

	void writer_update(fifo_writer * writer, int write){
		writer->shadow->write = write;
	}


    typedef struct {
        size_t readerc;
        fifo_reader **readers;
        FILE *stream;
    } input_actor_t;


    typedef struct {
        fifo_reader *reader;
        FILE *stream;
    } output_actor_t;

 fifo_reader *fifo_create(int ID) {
    fifo_reader * reader = (fifo_reader *)malloc(sizeof(fifo_reader));

    reader->ID = ID;
    reader->read = 0;
	reader->write = 0;
	reader->shadow = get_my_global_e_address(core_row, core_col);
	reader_init(reader);
	return reader;
   }

 bool fifo_tokens(fifo_reader* reader, int n)
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
	reader_update(reader);
}


void fifo_write(fifo_writer* writer, void * value, size_t size)
{
	memcpy(&writer->shadow->buffer[writer->write], (value), size);
	writer->write = (writer->write + 1) % (BUFFER_SIZE + 1);
}


char fifo_peek(fifo_reader* reader, int pos)
{
	return (reader->buffer[(reader->read + pos) % (BUFFER_SIZE + 1)]);
}


