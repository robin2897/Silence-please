import 'dart:async';

import '../model/appmodel.dart';
import './plugin.dart';

class Validator {
  bool checkIsEndTimeGreatorThanStartTime(AppModel model) {
    return (model.endTime.millisecondsSinceEpoch >
        model.startTime.millisecondsSinceEpoch);
  }

  Future<bool> checkTimesIsNotConflictingWithOther(AppModel model) async {
    var value = await PluginHandShake().all;
    if (value is List<AppModel>) {
      for (var _model in value) {
        var savedStartTime = _model.startTime;
        var savedEndTime = _model.endTime;
        var startTime = model.startTime;
        var endTime = model.endTime;
        if(startTime.isAfter(savedStartTime) && startTime.isBefore(savedEndTime)) {
          return false;
        }  
        if(endTime.isAfter(savedStartTime) && endTime.isBefore(savedEndTime)) {
          return false;
        }
      }
    }
    return true;
  }
}
