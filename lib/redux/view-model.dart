import 'package:meta/meta.dart';

import '../model/appmodel.dart';

typedef AddItem(AppModel model);
typedef UpdateItem(AppModel model);
typedef DeleteItem(AppModel model, int index);

class ViewModel {
  final AddItem addAction;
  final UpdateItem updateAction;
  final DeleteItem deleteAction;

  ViewModel.add({@required this.addAction})
      : deleteAction = null,
        updateAction = null;
  ViewModel.update({@required this.updateAction})
      : addAction = null,
        deleteAction = null;
  ViewModel.delete({@required this.deleteAction})
      : addAction = null,
        updateAction = null;
}
