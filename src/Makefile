CC = gcc 
FLAGS = -fPIC -Wall
HEADER = -I /home/andro/Open-TEE/emulator/include -I /home/andro/Open-TEE/TAs/include
TARGET_LIB = libeks_ta.so
DEBUG = -D TA_PLUGIN -D OT_LOGGING

SRCS = eks_ta.c
OBJ = $(patsubst %.c,%.o,$(SRCS))

all: $(TARGET_LIB)

%.o: %.c
	$(CC) $(DEBUG) -c $(FLAGS) $(HEADER) $< -o $@ 

$(TARGET_LIB): $(OBJ) 
	$(CC) -shared -o $@  $(OBJ)

clean:
	rm -f *.o *.so

cp:
	cp libeks_ta.so /home/andro/Open-TEE/gcc-debug/TAs

 

