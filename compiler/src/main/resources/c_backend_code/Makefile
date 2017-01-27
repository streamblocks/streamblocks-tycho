ESDK=$(EPIPHANY_HOME)

ELIBS=$(ESDK)/tools/host/lib
EINCS=$(ESDK)/tools/host/include

ELDF=$(ESDK)/bsps/current/internal.ldf

LIST=
EACTORS=$(addprefix -DACTOR_,$($(@:.elf=)))
HOSTACTORS=$(addprefix -DACTOR_,$(host))

CPPFLAGS = -O2 # l.ld #-Wl,-verbose
all:	$(LIST:=.srec)  main

main: host.c
	${eval host= ${addsuffix ),${addprefix $$(,$(LIST)}}}
	g++  host.c -DHOST $(CPPFLAGS) -DACTOR_output  -DEXCLUDE_ACTORS $(HOSTACTORS) -std=c++11 -o main -I $(EINCS) -L $(ELIBS) -le-hal -lrt


$(LIST:=.elf):
	e-g++ -O3 $(EACTORS) -T $(ELDF) -o $@ -le-lib

%.srec: %.elf
	e-objcopy --srec-forceS3 --output-target srec $(@:.srec=.elf) $@


clean:
	rm main *.elf *.srec