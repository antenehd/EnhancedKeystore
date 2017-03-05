CC = gcc 
LIB = -L /home/andro/Open-TEE/gcc-debug
HEADER = -I /home/andro/Open-TEE/libtee/include
TARGET = test_eks
SRCS = test_ca.c
OBJ = $(patsubst %.c,%.o,$(SRCS))
DEBUG = -D TA_PLUGIN -D OT_LOGGING

all: $(TARGET)

%.o: %.c
	$(CC) $(DEBUG) -c $(HEADER) $< -o $@  

$(TARGET) : $(OBJ)
	gcc -o $@ $(OBJ) $(LIB) -ltee

clean:
	rm -f *.o

cp:
	cp test_eks /home/andro/Open-TEE/gcc-debug

eprt:
	export LD_LIBRARY_PATH=/home/andro/Open-TEE/gcc-debug
	
