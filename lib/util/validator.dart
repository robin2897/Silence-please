import '../model/appmodel.dart';
import './plugin.dart';

class Validator {
  bool checkIsEndTimeGreatorThanStartTime(AppModel model) {
    return (model.endTime.millisecondsSinceEpoch >
        model.startTime.millisecondsSinceEpoch);
  }

  bool checkTimesIsNotConflictingWithOther(AppModel model) {
    PluginHandShake().all.then((value) {
      if (value is List<AppModel>) {
        value.forEach((_model) {
          var savedStartTime = _model.startTime.millisecondsSinceEpoch;
          var savedEndTime = _model.endTime.millisecondsSinceEpoch;
          var startTime = model.startTime.millisecondsSinceEpoch;
          var endTime = model.endTime.millisecondsSinceEpoch;
          if(startTime <= savedStartTime && startTime >= savedEndTime) {
            return false;
          }  
          if(endTime <= savedStartTime && endTime >= endTime) {
            return false;
          }
        });
      }
      return true;
    });
    return true;
  }
}
