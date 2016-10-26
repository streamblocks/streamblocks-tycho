ESDK=$(EPIPHANY_HOME)

ELIBS=$(ESDK)/tools/host/lib
EINCS=$(ESDK)/tools/host/include

ELDF=$(ESDK)/bsps/current/internal.ldf

LIST=core1 core2
EACTORS=$(addprefix -DACTOR_,$($(@:.elf=)))
HOSTACTORS=$(addprefix -DACTOR_,$(host))

core1 = Add
core2 = Delay


CPPFLAGS = -O2 # l.ld #-Wl,-verbose
all:	$(LIST:=.srec)  main

main: host.cpp shared_data.h
	${eval host= ${addsuffix ),${addprefix $$(,$(LIST)}}}
	g++ fifo.cpp host.cpp -DHOST $(CPPFLAGS) -DACTOR_output  -DEXCLUDE_ACTORS $(HOSTACTORS) -std=c++11 -o main -I $(EINCS) -L $(ELIBS) -le-hal -lrt


$(LIST:=.elf): epiphany_actors.cpp shared_data.h
	e-g++ -O3 $(EACTORS) -ffast-math -T $(ELDF) fifo.cpp epiphany_actors.cpp -o $@ -le-lib

%.srec: %.elf
	e-objcopy --srec-forceS3 --output-target srec $(@:.srec=.elf) $@


clean:
	rm main *.elf *.srec