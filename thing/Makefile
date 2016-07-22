ifdef DEBUG
CFLAGS += -g -DDEBUG
endif
CFLAGS += -std=gnu89 -Wall -pedantic -pthread -DKII_JSON_FIXED_TOKEN_NUM=256 -DKII_PUSH_KEEP_ALIVE_INTERVAL_SECONDS=300

SDK_DIR = ../../thing-if-ThingSDK
LIBS = -lssl -lcrypto -lpthread -lkiithingifsdk
LD_FLAGS = -L$(SDK_DIR)
SOURCES = $(wildcard *.c)
TARGET = hellothingif
INCLUDES = -I$(SDK_DIR) -I$(SDK_DIR)/kii/kii-core -I$(SDK_DIR)/kii/kii-core/linux -I$(SDK_DIR)/kii/kii -I$(SDK_DIR)/kii/kii_json/include -I$(SDK_DIR)/kii/lib/jsmn


all: clean $(TARGET) $(DOCTARGET)

$(TARGET):
	gcc $(CFLAGS) $(SOURCES) $(LIBS) $(LD_FLAGS) $(INCLUDES) -o $@

clean:
	touch $(TARGET)
	rm $(TARGET)
	rm -rf $(DOCTARGET)

.PHONY: all clean copy
