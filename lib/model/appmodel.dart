import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:meta/meta.dart';

part 'appmodel.g.dart';

@JsonSerializable()
class AppModel extends Object with _$AppModelSerializerMixin {
  @JsonKey(
      name: "start_time",
      toJson: _fromDateTimeToMillisecond,
      fromJson: _fromMillisecondTomDateTime)
  DateTime startTime;
  @JsonKey(
      name: "end_time",
      toJson: _fromDateTimeToMillisecond,
      fromJson: _fromMillisecondTomDateTime)
  DateTime endTime;
  @JsonKey(name: "is_active")
  bool isActive;
  @JsonKey(name: "is_silent")
  bool isSilent;
  @JsonKey(name: "is_vibrate")
  bool isVibrate;
  @JsonKey(nullable: true)
  String id;
  List<int> days;
  @JsonKey(ignore: true)
  bool isExpanded = false;

  AppModel(this.startTime, this.endTime, this.days, this.isSilent,
      this.isVibrate, this.isActive, this.id,
      [this.isExpanded = false]);

  AppModel.fromTimeOfDay(
      {@required TimeOfDay startTime,
      @required TimeOfDay endTime,
      @required this.isActive,
      @required this.days,
      @required this.isSilent,
      @required this.isVibrate,
      this.isExpanded: false}) {
    this.startTime = new DateTime(
        1970, DateTime.january, 1, startTime.hour, startTime.minute);
    this.endTime =
        new DateTime(1970, DateTime.january, 1, endTime.hour, endTime.minute);
  }

  factory AppModel.fromJson(Map<String, dynamic> json) =>
      _$AppModelFromJson(json);

  @override
  int get hashCode {
    return startTime.hashCode +
        endTime.hashCode +
        isActive.hashCode +
        days.hashCode +
        isSilent.hashCode +
        isVibrate.hashCode +
        id.hashCode;
  }

  @override
  bool operator ==(other) {
    return other.hashCode == this.hashCode;
  }
}

int _fromDateTimeToMillisecond(DateTime time) {
  return time.millisecondsSinceEpoch;
}

DateTime _fromMillisecondTomDateTime(int millisecond) {
  return DateTime.fromMillisecondsSinceEpoch(millisecond);
}
