import 'package:redux/redux.dart';

import '../model/appmodel.dart';
import '../util/plugin.dart';
import 'state-manager.dart';

void actionsMiddleware(
    Store<AppState> store, action, NextDispatcher next) async {
  if (action is AddAction) {
    var result = await PluginHandShake().insertDB(model: action.model);
    if (!(result is int)) store.dispatch(new ErrorThrown(message: result));

    var items = await PluginHandShake().all;
    if (items is List<AppModel>)
      store.dispatch(new UpdateListAction(model: items.last));
    else
      store.dispatch(new ErrorThrown(message: items));
  } else if (action is UpdateAction) {
    var result = await PluginHandShake().updateDB(model: action.model);
    if (!(result is int)) store.dispatch(new ErrorThrown(message: result));
  } else if (action is DeleteAction) {
    var result = await PluginHandShake().deleteDB(model: action.model);
    if (!(result is int)) store.dispatch(new ErrorThrown(message: result));
  } else if (action is InitiateAction) {
    var result = await PluginHandShake().all;
    if (result is List<AppModel>)
      store.dispatch(new RecreateAction(list: result));
    else
      store.dispatch(new ErrorThrown(message: result));
  }

  next(action);
}
