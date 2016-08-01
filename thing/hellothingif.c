#include "hellothingif.h"

#include <kii_thing_if.h>
#include <kii_json.h>

#include <string.h>
#include <stdio.h>
#include <getopt.h>
#include <stdlib.h>

#include <pthread.h>

typedef struct prv_smartlight_t {
    kii_json_boolean_t power;
    int brightness;
} prv_smartlight_t;

static prv_smartlight_t m_smartlight;
static pthread_mutex_t m_mutex;
static int m_motion = 0;

static kii_json_parse_result_t prv_json_read_object(
        const char* json,
        size_t json_len,
        kii_json_field_t* fields,
        char error[EMESSAGE_SIZE + 1])
{
    kii_json_t kii_json;
    kii_json_resource_t* resource_pointer = NULL;

    memset(&kii_json, 0, sizeof(kii_json));
    kii_json.resource = resource_pointer;
    kii_json.error_string_buff = error;
    kii_json.error_string_length = EMESSAGE_SIZE + 1;

    return kii_json_read_object(&kii_json, json, json_len, fields);
}

static kii_bool_t prv_get_smartlight_info(prv_smartlight_t* smartlight)
{
    if (pthread_mutex_lock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    smartlight->power = m_smartlight.power;
    smartlight->brightness = m_smartlight.brightness;
    if (pthread_mutex_unlock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    return KII_TRUE;
}

static kii_bool_t prv_set_smartlight_info(const prv_smartlight_t* smartlight)
{
    if (pthread_mutex_lock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    m_smartlight.power = smartlight->power;
    m_smartlight.brightness = smartlight->brightness;
    if (pthread_mutex_unlock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    return KII_TRUE;
}

static kii_bool_t prv_get_motion_sensor(int *motion)
{
    if (pthread_mutex_lock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    m_motion = (m_motion + 1) % 11;
    *motion = m_motion;
    if (pthread_mutex_unlock(&m_mutex) != 0) {
        return KII_FALSE;
    }
    return KII_TRUE;
}

static void prv_action_error(char error[EMESSAGE_SIZE + 1], const char *message)
{
    memset(error, 0x00, EMESSAGE_SIZE + 1);
    strncpy(error, message, EMESSAGE_SIZE);
    printf("Error: %s\n", message);
}

static kii_bool_t action_handler(
        const char* schema,
        int schema_version,
        const char* action_name,
        const char* action_params,
        char error[EMESSAGE_SIZE + 1])
{
    prv_smartlight_t smartlight;

    printf("Schema=%s, schema version=%d, action name=%s, action params=%s\n",
            schema, schema_version, action_name, action_params);

    if (strcmp(schema, SCHEMA_NAME) != 0 || SCHEMA_VERSION != 1) {
        prv_action_error(error, "Invalid schema.");
        return KII_FALSE;
    }

    memset(&smartlight, 0x00, sizeof(smartlight));
    if (strcmp(action_name, "turnPower") == 0) {
        kii_json_field_t fields[2];

        memset(fields, 0x00, sizeof(fields));
        fields[0].path = "/power";
        fields[0].type = KII_JSON_FIELD_TYPE_BOOLEAN;
        fields[1].path = NULL;
        if (prv_json_read_object(action_params, strlen(action_params),
                        fields, error) !=  KII_JSON_PARSE_SUCCESS) {
            prv_action_error(error, "Invalid turnPower in JSON.");
            return KII_FALSE;
        }
        if (prv_get_smartlight_info(&smartlight) == KII_FALSE) {
            prv_action_error(error, "Failed to get smartlight info.");
            return KII_FALSE;
        }
        smartlight.power = fields[0].field_copy.boolean_value;
        if (prv_set_smartlight_info(&smartlight) == KII_FALSE) {
            prv_action_error(error, "Failed to set smartlight info.");
            return KII_FALSE;
        }
    } else if (strcmp(action_name, "setBrightness") == 0) {
        kii_json_field_t fields[2];

        memset(fields, 0x00, sizeof(fields));
        fields[0].path = "/brightness";
        fields[0].type = KII_JSON_FIELD_TYPE_INTEGER;
        fields[1].path = NULL;
        if(prv_json_read_object(action_params, strlen(action_params),
                        fields, error) !=  KII_JSON_PARSE_SUCCESS) {
            prv_action_error(error, "Invalid brightness in JSON.");
            return KII_FALSE;
        }
        if (prv_get_smartlight_info(&smartlight) == KII_FALSE) {
            prv_action_error(error, "Failed to get smartlight info.");
            return KII_FALSE;
        }
        if (smartlight.brightness == 100 && fields[0].field_copy.int_value == 100) {
            prv_action_error(error, "Bulb is overheating.");
            return KII_FALSE;
        }
        smartlight.brightness = fields[0].field_copy.int_value;
        if (prv_set_smartlight_info(&smartlight) == KII_FALSE) {
            prv_action_error(error, "Failed to set smartlight info.");
            return KII_FALSE;
        }
    } else {
        prv_action_error(error, "Invalid action.");
        return KII_FALSE;
    }

    if (smartlight.power) {
        printf("Light: Power on, Brightness: %d\n", smartlight.brightness);
    } else {
        printf("Light: Power off\n");
    }

    return KII_TRUE;
}

static kii_bool_t state_handler(
        kii_t* kii,
        KII_THING_IF_WRITER writer)
{
    char buf[256];
    int motion;
    prv_smartlight_t smartlight;
    memset(&smartlight, 0x00, sizeof(smartlight));
    if (prv_get_smartlight_info(&smartlight) == KII_FALSE) {
        printf("Failed to lock/unlock mutex.\n");
        return KII_FALSE;
    }
    if (prv_get_motion_sensor(&motion) == KII_FALSE) {
        printf("Failed to lock/unlock mutex.\n");
        return KII_FALSE;
    }

    if ((*writer)(kii, "{\"power\":") == KII_FALSE) {
        return KII_FALSE;
    }
    if ((*writer)(kii, smartlight.power == KII_JSON_TRUE
                    ? "true," : "false,") == KII_FALSE) {
        return KII_FALSE;
    }

    if ((*writer)(kii, "\"brightness\":") == KII_FALSE) {
        return KII_FALSE;
    }
    sprintf(buf, "%d,", smartlight.brightness);
    if ((*writer)(kii, buf) == KII_FALSE) {
        return KII_FALSE;
    }

    if ((*writer)(kii, "\"motion\":") == KII_FALSE) {
        return KII_FALSE;
    }
    sprintf(buf, "%d}", motion);
    if ((*writer)(kii, buf) == KII_FALSE) {
        return KII_FALSE;
    }

    printf("Sending the state\n");

    return KII_TRUE;
}

int main(int argc, char** argv)
{
    kii_bool_t result;
    kii_thing_if_command_handler_resource_t command_handler_resource;
    kii_thing_if_state_updater_resource_t state_updater_resource;
    char command_handler_buff[EX_COMMAND_HANDLER_BUFF_SIZE];
    char state_updater_buff[EX_STATE_UPDATER_BUFF_SIZE];
    char mqtt_buff[EX_MQTT_BUFF_SIZE];
    kii_thing_if_t kii_thing_if;

    char *vendorThingID, *thingPassword;

    if (argc != 3) {
        printf("hellothingif {vendor thing id} {thing password}\n");
        exit(1);
    }

    vendorThingID = argv[1];
    thingPassword = argv[2];

    if (pthread_mutex_init(&m_mutex, NULL) != 0) {
        printf("Failed to get mutex.\n");
        exit(1);
    }

    /* prepare for the command handler */
    memset(&command_handler_resource, 0x00, sizeof(command_handler_resource));
    command_handler_resource.buffer = command_handler_buff;
    command_handler_resource.buffer_size =
        sizeof(command_handler_buff) / sizeof(command_handler_buff[0]);
    command_handler_resource.mqtt_buffer = mqtt_buff;
    command_handler_resource.mqtt_buffer_size =
        sizeof(mqtt_buff) / sizeof(mqtt_buff[0]);
    command_handler_resource.action_handler = action_handler;
    command_handler_resource.state_handler = state_handler;

    /* prepare for the state updater */
    memset(&state_updater_resource, 0x00, sizeof(state_updater_resource));
    state_updater_resource.buffer = state_updater_buff;
    state_updater_resource.buffer_size =
        sizeof(state_updater_buff) / sizeof(state_updater_buff[0]);
    state_updater_resource.period = EX_STATE_UPDATE_PERIOD;
    state_updater_resource.state_handler = state_handler;

    /* initialize the SDK */
    result = init_kii_thing_if(&kii_thing_if, EX_APP_ID, EX_APP_KEY, EX_APP_SITE,
            &command_handler_resource, &state_updater_resource, NULL);
    if (result == KII_FALSE) {
        printf("Failed to initialize the SDK.\n");
        exit(1);
    }

    result = onboard_with_vendor_thing_id(&kii_thing_if, vendorThingID, thingPassword, THING_TYPE, THING_PROPERTIES);
    if (result == KII_FALSE) {
        printf("Failed to onboard the thing. %d, %s\n", kii_thing_if.command_handler.kii_core.response_code, kii_thing_if.command_handler.kii_core.response_body);
        exit(1);
    }

    printf("Waiting for commands\n");

    while(1){}; /* run the program forever */

    /*
     * the destroy function is not implemented because this sample 
     * application keeps mutex throughout the applicatoin process
     * pthread_mutex_destroy(&m_mutex);
    */
}

/* vim: set ts=4 sts=4 sw=4 et fenc=utf-8 ff=unix: */
