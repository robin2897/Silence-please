// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'state-manager.dart';

// **************************************************************************
// Generator: JsonSerializableGenerator
// **************************************************************************

AppState _$AppStateFromJson(Map<String, dynamic> json) => new AppState(
    items: (json['items'] as List)
        ?.map((e) =>
            e == null ? null : new AppModel.fromJson(e as Map<String, dynamic>))
        ?.toList());

abstract class _$AppStateSerializerMixin {
  List<AppModel> get items;
  Map<String, dynamic> toJson() => <String, dynamic>{'items': items};
}
