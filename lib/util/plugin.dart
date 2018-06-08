import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:meta/meta.dart';
import 'package:uuid/uuid.dart';

import '../model/appmodel.dart';
import '../redux/state-manager.dart';

class PluginHandShake {
  static const platform = MethodChannel("com.inc.rims.silenceplease/database");

  Future<dynamic> insertDB({@required AppModel model}) async {
    try {
      var uuid = new Uuid();
      model.id = uuid.v1();
      var encodedModel = json.encode(model.toJson());
      return await platform.invokeMethod(
          "insertDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> deleteDB({@required AppModel model}) async {
    try {
      var encodedModel = json.encode(model);
      return await platform.invokeMethod(
        "deleteDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> updateDB({@required AppModel model}) async {
    try {
      var encodedModel = json.encode(model);
      return await platform.invokeMethod(
        "updateDB", <String, dynamic>{"model": encodedModel});
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> get all async {
    try {
      String encodedResult = await platform.invokeMethod("getAllDB");
      return new AppState.fromJson(json.decode(encodedResult)).items;
    } catch (e) {
      return e.toString();
    }
  }

  Future<dynamic> get nextDB async {
    try {
      String encodedResult = await platform.invokeMethod("getNextDB");
      var decodedResult = json.decode(encodedResult);
      return new AppModel.fromJson(decodedResult);
    } catch (e) {
      return e.toString();
    }
  }

  void toggleEnable() async {
    try {
      await platform.invokeMethod("enableToggle");
    } catch (e) {
    }
  }
}
