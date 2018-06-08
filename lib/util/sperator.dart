import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

class TimeExtractor {
  final String time;
  final String suffix;
  TimeExtractor({this.time, this.suffix});
}

TimeExtractor timeExtractor({@required TimeOfDay rawTime, @required BuildContext context}) {
  String suffix = "";
  String time = rawTime.format(context);
  if (time.contains('AM') || time.contains('PM')) {
    if (time.contains('AM')) {
      suffix = 'AM';
      time = time.replaceAll('AM', '');
    } else if (time.contains('PM')) {
      suffix = 'PM';
      time = time.replaceAll('PM', '');
    }
  }
  return new TimeExtractor(time: time, suffix: suffix);
}
