// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'appmodel.dart';

// **************************************************************************
// Generator: JsonSerializableGenerator
// **************************************************************************

AppModel _$AppModelFromJson(Map<String, dynamic> json) => new AppModel(
    json['start_time'] == null
        ? null
        : _fromMillisecondTomDateTime(json['start_time'] as int),
    json['end_time'] == null
        ? null
        : _fromMillisecondTomDateTime(json['end_time'] as int),
    (json['days'] as List)?.map((e) => e as int)?.toList(),
    json['is_silent'] as bool,
    json['is_vibrate'] as bool,
    json['is_active'] as bool,
    json['id'] as String);

abstract class _$AppModelSerializerMixin {
  DateTime get startTime;
  DateTime get endTime;
  bool get isActive;
  bool get isSilent;
  bool get isVibrate;
  String get id;
  List<int> get days;
  Map<String, dynamic> toJson() => <String, dynamic>{
        'start_time':
            startTime == null ? null : _fromDateTimeToMillisecond(startTime),
        'end_time':
            endTime == null ? null : _fromDateTimeToMillisecond(endTime),
        'is_active': isActive,
        'is_silent': isSilent,
        'is_vibrate': isVibrate,
        'id': id,
        'days': days
      };
}
