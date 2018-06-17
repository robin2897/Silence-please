class AppConfig {
  static const SMS_SERVICE_ENABLE = "SMS_SERVICE";
  static const SMS_SERVICE_MESSAGE = "SMS_SERVICE_MESSAGE";
  static const SMS_SERVICE_ATTEMPTS = "SMS_SERVICE_ATTEMPTS";
  static const WHITE_LIST_SERVICE = "WHITE_LIST_SERVICE";
  static const SILENCE_IS_ENABLE = "IS_ENABLE";
  static const SMS_SERVICE_ENABLE_TEMP = "SMS_SERVICE_TEMP";
  static const IS_SILENCE_ACTIVE = "IS_SILENCE_ACTIVE";
  static const VERSION = "1.1.1-preview";
  static const WATERMARK = "\n\nThis message is send by Silence Please" +
      "\nDownload from play store ";

  static Flavor flavor;
}

enum Flavor { PAID, FREE }
