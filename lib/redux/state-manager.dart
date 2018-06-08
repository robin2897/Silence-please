import 'package:json_annotation/json_annotation.dart';
import 'package:meta/meta.dart';

import '../model/appmodel.dart';

part 'state-manager.g.dart';

@JsonSerializable()
class AppState extends Object with _$AppStateSerializerMixin {
  final List<AppModel> items;

  AppState({@required this.items});
  AppState.init() : items = <AppModel>[];

  factory AppState.fromJson(Map<String, dynamic> json) =>
      _$AppStateFromJson(json);

  @override
  int get hashCode {
    int hash = 0;
    for (var item in items) {
      hash = hash + item.hashCode;
    }
    return hash;
  }

  @override
  bool operator ==(other) {
    return other.hashCode == this.hashCode;
  }
}

AppState reducer(AppState state, action) {
  if (action is UpdateListAction) {
    var items = state.items..add(action.model);
    return AppState(items: items);
  } else if (action is DeleteAction) {
    var items = state.items..removeAt(action.index);
    return AppState(items: items);
  } else if (action is RecreateAction) {
    return AppState(items: state.items..addAll(action.list));
  }

  return AppState(items: []..addAll(state.items));
}

class AddAction {
  final AppModel model;
  AddAction({@required this.model});
}

class DeleteAction {
  final AppModel model;
  final int index;
  DeleteAction({@required this.model, @required this.index});
}

class UpdateAction {
  final AppModel model;
  UpdateAction({@required this.model});
}

class UpdateListAction {
  final AppModel model;
  UpdateListAction({@required this.model});
}

class RecreateAction {
  final List<AppModel> list;
  RecreateAction({@required this.list});
}

class InitiateAction {}

class ErrorThrown {
  final String message;
  ErrorThrown({@required this.message});
}
