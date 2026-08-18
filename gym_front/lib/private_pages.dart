import 'package:flutter/material.dart';
import 'main.dart';

class PrivateBookingPage extends StatelessWidget {
  const PrivateBookingPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('预约私教')),
    body: FutureBuilder<List<dynamic>>(
      future: getList('/coach/availability'),
      builder: (_, snapshot) {
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        if (snapshot.data!.isEmpty)
          return const Center(child: Text('暂时没有可预约的私教时间'));
        return ListView(
          children: snapshot.data!
              .map(
                (x) => Card(
                  child: ListTile(
                    leading: const Icon(Icons.person_outline, color: purple),
                    title: Text('${x['coachName']}'),
                    subtitle: Text('${x['startTime']} 至 ${x['endTime']}'),
                    trailing: FilledButton(
                      onPressed: () async {
                        try {
                          await Api.post(
                            '/private-bookings/${session.username}/${x['id']}',
                            {},
                          );
                          if (context.mounted)
                            showToast(context, '预约申请已提交，等待管理员审核');
                        } catch (e) {
                          if (context.mounted) showToast(context, '$e');
                        }
                      },
                      child: const Text('预约'),
                    ),
                  ),
                ),
              )
              .toList(),
        );
      },
    ),
  );
}

class AvailabilityPage extends StatefulWidget {
  const AvailabilityPage(this.session, {super.key});
  final Session session;
  @override
  State<AvailabilityPage> createState() => _AvailabilityPageState();
}

class _AvailabilityPageState extends State<AvailabilityPage> {
  DateTime? start;
  DateTime? end;
  bool loading = false;

  String _fmt(DateTime dt) {
    String two(int n) => n.toString().padLeft(2, '0');
    return '${dt.year}-${two(dt.month)}-${two(dt.day)}'
        'T${two(dt.hour)}:${two(dt.minute)}:${two(dt.second)}';
  }

  Future<void> _pickStart() async {
    final d = await showDatePicker(
      context: context,
      initialDate: start ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    if (d == null || !mounted) return;
    final t = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(start ?? DateTime.now()),
    );
    if (t == null) return;
    setState(() => start = DateTime(d.year, d.month, d.day, t.hour, t.minute));
  }

  Future<void> _pickEnd() async {
    final d = await showDatePicker(
      context: context,
      initialDate: end ?? start ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 90)),
    );
    if (d == null || !mounted) return;
    final t = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(end ?? DateTime.now()),
    );
    if (t == null) return;
    setState(() => end = DateTime(d.year, d.month, d.day, t.hour, t.minute));
  }

  Future<void> save() async {
    if (start == null || end == null) {
      showToast(context, '请选择开始和结束时间');
      return;
    }
    if (!end!.isAfter(start!)) {
      showToast(context, '结束时间必须晚于开始时间');
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/coach/${widget.session.username}/availability', {
        'startTime': _fmt(start!),
        'endTime': _fmt(end!),
      });
      if (mounted) {
        showToast(context, '空闲时间已发布');
        setState(() {
          start = null;
          end = null;
        });
      }
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('发布空闲时间')),
    body: Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          const Text('选择你可以提供私教服务的时间段'),
          const SizedBox(height: 12),
          ListTile(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: const BorderSide(color: Colors.grey),
            ),
            leading: const Icon(Icons.event),
            title: Text(start == null ? '开始时间' : '开始：${_fmt(start!)}'),
            onTap: _pickStart,
          ),
          const SizedBox(height: 12),
          ListTile(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: const BorderSide(color: Colors.grey),
            ),
            leading: const Icon(Icons.event),
            title: Text(end == null ? '结束时间' : '结束：${_fmt(end!)}'),
            onTap: _pickEnd,
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: loading ? null : save,
            child: Text(loading ? '发布中...' : '发布'),
          ),
        ],
      ),
    ),
  );
}
