import 'package:flutter/material.dart';
import 'main.dart';

class PrivateBookingPage extends StatelessWidget {
  const PrivateBookingPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('预约私教')),
    body: FutureBuilder<List<dynamic>>(
      future: getList('/api/coach/availability'),
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
                            '/api/private-bookings/${session.username}/${x['id']}',
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
  final start = TextEditingController();
  final end = TextEditingController();
  bool loading = false;
  @override
  void dispose() {
    start.dispose();
    end.dispose();
    super.dispose();
  }

  Future<void> save() async {
    if (start.text.trim().isEmpty || end.text.trim().isEmpty) {
      showToast(context, '请输入开始和结束时间');
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/api/coach/${widget.session.username}/availability', {
        'startTime': start.text.trim(),
        'endTime': end.text.trim(),
      });
      if (mounted) {
        showToast(context, '空闲时间已发布');
        start.clear();
        end.clear();
      }
    } catch (e) {
      if (mounted) showToast(context, '时间格式：2026-08-20T10:00');
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
          const Text('使用格式：2026-08-20T10:00'),
          const SizedBox(height: 12),
          TextField(
            controller: start,
            decoration: const InputDecoration(
              labelText: '开始时间',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: end,
            decoration: const InputDecoration(
              labelText: '结束时间',
              border: OutlineInputBorder(),
            ),
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
