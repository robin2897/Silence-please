import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:meta/meta.dart';
import 'package:uuid/uuid.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';

class PluginHandShake {
  static const methodChannel =
      MethodChannel("com.inc.rims.silenceplease/database");
  static const serviceEventChannel =
      EventChannel("com.inc.rims.silenceplease/service-event");
  static const contactEventChannel =
      EventChannel("com.inc.rims.silenceplease/contacts-event");

  static const SMS_SERVICE = "sms-service";
  static const NOTIFICATION_POLICY = "notification-policy";
  static const WHITE_LIST_SERVICE_ENABLE = "contacts";
  static const WHITE_LIST_OP_INSERT = "insert";
  static const WHITE_LIST_OP_DELETE = "delete";
  static const WHITE_LIST_OP_ALL = "getAll";

  Future<dynamic> insertDB({@required AppModel model}) async {
    try {
      var uuid = new Uuid();
      model.id = uuid.v1();
      var encodedModel = json.encode(model.toJson());
      return await methodChannel
          .invokeMethod("insertDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> deleteDB({@required AppModel model}) async {
    try {
      var encodedModel = json.encode(model);
      return await methodChannel
          .invokeMethod("deleteDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> updateDB({@required AppModel model}) async {
    try {
      var encodedModel = json.encode(model);
      return await methodChannel
          .invokeMethod("updateDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> get all async {
    try {
      String encodedResult = await methodChannel.invokeMethod("getAllDB");
      return new AppState.fromJson(json.decode(encodedResult)).items;
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> get nextDB async {
    try {
      String encodedResult = await methodChannel.invokeMethod("getNextDB");
      var decodedResult = json.decode(encodedResult);
      return new AppModel.fromJson(decodedResult);
    } catch (e) {
      return e.toString();
    }
  }

  void toggleEnable() async {
    try {
      await methodChannel.invokeMethod("enableToggle");
    } catch (e) {}
  }

  Future<String> checkPermission(String permission) async {
    try {
      String result = await methodChannel.invokeMethod(
          "checkPermission", <String, dynamic>{"permission": permission});
      print("checkPermission: $permission => $result");
      return result;
    } catch (e) {
      return "";
    }
  }

  void redirectPermisionsSettings() async {
    try {
      await methodChannel.invokeMethod("redirectPermissionSetting");
    } catch (e) {}
  }

  void searchContact(String query) async {
    try {
      await methodChannel
          .invokeMethod("queryContact", <String, dynamic>{"query": query});
    } catch (e) {}
  }

  Future<dynamic> whiteListOp(String op, [String arg, String uuid]) async {
    try {
      return await methodChannel.invokeMethod(
          "whitelistOp", <String, dynamic>{"op": op, "arg": arg, "uuid": uuid});
    } catch (e) {
      return null;
    }
  }
}
