#ifndef __example
#define __example

#ifdef __cplusplus
extern 'C' {
#endif

/* Go to https:/developer.kii.com and create your app! */
const char EX_APP_ID[] = "___APP_ID___";
const char EX_APP_KEY[] = "___APP_KEY___";
const char EX_APP_SITE[] = "___SITE___";

const char THING_TYPE[] = "HelloThingIF-SmartLED";
const char THING_PROPERTIES[] = "{}";
const char SCHEMA_NAME[] = "HelloThingIF-Schema";
#define SCHEMA_VERSION 1

#define EX_COMMAND_HANDLER_BUFF_SIZE 4096
#define EX_STATE_UPDATER_BUFF_SIZE 4096
#define EX_MQTT_BUFF_SIZE 2048
#define EX_STATE_UPDATE_PERIOD 60

#ifdef __cplusplus
}
#endif

#endif
