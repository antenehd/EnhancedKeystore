CC = gcc 
LIB = /home/andro/Open-TEE/gcc-debug
HEADER = -I /home/andro/Open-TEE/libtee/include
TARGET = test_eks
SRCS = test_eks.c
OBJ = $(patsubst %.c,%.o,$(SRCS))
DEBUG = -D TA_PLUGIN -D OT_LOGGING

all: $(TARGET)

%.o: %.c
	$(CC) $(DEBUG) -c $(HEADER) $< -o $@  

$(TARGET) : $(OBJ)
	gcc -o $@ $(OBJ) -L $(LIB) -ltee -Wl,-rpath=$(LIB)

clean:
	rm -f *.o

cp:
	cp test_eks /home/andro/Open-TEE/gcc-debug

export:
	sh export LD_LIBRARY_PATH=/home/andro/Open-TEE/gcc-debug
	
