import 'dart:async';
import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:qr_flutter/qr_flutter.dart';
import 'coach_apply_page.dart';
import 'private_pages.dart';

const purple = Color(0xff7357d8);
const blue = Color(0xff3979d8);
const ink = Color(0xff17152a);
const pale = Color(0xfff1efff);

void main() => runApp(const StarGymApp());

class Api {
  static const base = 'http://10.0.2.2:8080';
  static Future<dynamic> get(String path) async =>
      decode(await http.get(Uri.parse('$base$path')));
  static Future<dynamic> post(String path, Object body) async => decode(
    await http.post(
      Uri.parse('$base$path'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(body),
    ),
  );
  static Future<dynamic> put(String path) async =>
      decode(await http.put(Uri.parse('$base$path')));
  static dynamic decode(http.Response response) {
    dynamic body;
    if (response.bodyBytes.isNotEmpty) {
      body = jsonDecode(utf8.decode(response.bodyBytes));
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      final msg = body is Map && body['msg'] != null
          ? '${body['msg']}'
          : '\u8bf7\u6c42\u5931\u8d25\uff1a${response.statusCode}';
      throw Exception(msg);
    }
    if (body is Map && body.containsKey('code') && body.containsKey('data')) {
      return body['data'];
    }
    return body;
  }
}

class Session {
  const Session(this.username, this.id, this.role);
  final String username;
  final int id;
  final String role;
}

Future<List<dynamic>> getList(String path) async {
  final result = await Api.get(path);
  return result is List ? List<dynamic>.from(result) : [];
}

void showToast(BuildContext context, String message) => ScaffoldMessenger.of(
  context,
).showSnackBar(SnackBar(content: Text(message)));
void push(BuildContext context, Widget page) =>
    Navigator.push(context, MaterialPageRoute(builder: (_) => page));

class StarGymApp extends StatelessWidget {
  const StarGymApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    theme: ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: purple),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.white,
        foregroundColor: ink,
      ),
    ),
    home: const WelcomePage(),
  );
}

class WelcomePage extends StatelessWidget {
  const WelcomePage({super.key});
  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Spacer(),
            const Text(
              '\u6b22\u8fce\u6765\u5230',
              style: TextStyle(fontSize: 23),
            ),
            const Text(
              'Star GYM',
              style: TextStyle(
                fontSize: 40,
                color: purple,
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              '\u5728\u8fd9\u91cc\uff0c\u8ba4\u771f\u8fd0\u52a8\uff0c\u4e5f\u8ba4\u771f\u751f\u6d3b',
              style: TextStyle(color: Colors.black54),
            ),
            const SizedBox(height: 28),
            Center(
              child: Image.asset('assets/images/onboarding.png', height: 250),
            ),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              height: 52,
              child: FilledButton(
                onPressed: () => push(context, const AuthPage()),
                child: const Text('\u5f00\u59cb\u4f7f\u7528'),
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class AuthPage extends StatefulWidget {
  const AuthPage({super.key});
  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final username = TextEditingController();
  final password = TextEditingController();
  final phone = TextEditingController();
  bool isRegister = false;
  bool loading = false;
  @override
  void dispose() {
    username.dispose();
    password.dispose();
    phone.dispose();
    super.dispose();
  }

  Future<void> submit() async {
    if (username.text.trim().isEmpty || password.text.isEmpty) {
      showToast(context, '\u8bf7\u8f93\u5165\u8d26\u53f7\u548c\u5bc6\u7801');
      return;
    }
    setState(() => loading = true);
    try {
      final data = Map<String, dynamic>.from(
        await Api.post(
          isRegister ? '/user/register' : '/user/login',
          {
            'username': username.text.trim(),
            'password': password.text,
            if (isRegister) 'phone': phone.text.trim(),
          },
        ) as Map,
      );
      if (!mounted) return;
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(
          builder: (_) => AppShell(
            Session(
              '${data['username'] ?? username.text.trim()}',
              (data['id'] as num).toInt(),
              '${data['role'] ?? 'USER'}',
            ),
          ),
        ),
        (_) => false,
      );
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: pale,
    body: SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(28, 42, 28, 24),
            child: Text(
              '\u4f60\u597d\n\u6b22\u8fce\u6765\u5230 Star GYM',
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w800,
                color: ink,
              ),
            ),
          ),
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(26),
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
              ),
              child: ListView(
                children: [
                  SegmentedButton<bool>(
                    segments: const [
                      ButtonSegment(value: false, label: Text('\u767b\u5f55')),
                      ButtonSegment(value: true, label: Text('\u6ce8\u518c')),
                    ],
                    selected: {isRegister},
                    onSelectionChanged: (value) =>
                        setState(() => isRegister = value.first),
                  ),
                  const SizedBox(height: 20),
                  TextField(
                    controller: username,
                    decoration: const InputDecoration(
                      labelText: '\u8d26\u53f7',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: password,
                    obscureText: true,
                    decoration: const InputDecoration(
                      labelText: '\u5bc6\u7801',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  if (isRegister) ...[
                    const SizedBox(height: 12),
                    TextField(
                      controller: phone,
                      decoration: const InputDecoration(
                        labelText: '\u624b\u673a\u53f7',
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ],
                  const SizedBox(height: 24),
                  SizedBox(
                    height: 50,
                    child: FilledButton(
                      onPressed: loading ? null : submit,
                      child: Text(
                        loading
                            ? '\u8bf7\u7a0d\u5019...'
                            : isRegister
                            ? '\u6ce8\u518c\u5e76\u8fdb\u5165'
                            : '\u767b\u5f55',
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    ),
  );
}

class AppShell extends StatefulWidget {
  const AppShell(this.session, {super.key});
  final Session session;
  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  int selected = 0;
  @override
  Widget build(BuildContext context) {
    final coach = widget.session.role == 'COACH';
    final pages = coach
        ? [CoachHome(widget.session), NoticePage(widget.session)]
        : [
            MemberHome(widget.session),
            CoursePage(widget.session),
            ProfilePage(widget.session),
          ];
    final items = coach
        ? const [
            NavigationDestination(
              icon: Icon(Icons.calendar_month_outlined),
              label: '\u65e5\u7a0b',
            ),
            NavigationDestination(
              icon: Icon(Icons.notifications_none),
              label: '\u901a\u77e5',
            ),
          ]
        : const [
            NavigationDestination(
              icon: Icon(Icons.home_outlined),
              label: '\u9996\u9875',
            ),
            NavigationDestination(
              icon: Icon(Icons.fitness_center_outlined),
              label: '\u8bfe\u7a0b',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline),
              label: '\u6211\u7684',
            ),
          ];
    return Scaffold(
      body: pages[selected],
      bottomNavigationBar: NavigationBar(
        selectedIndex: selected,
        onDestinationSelected: (value) => setState(() => selected = value),
        destinations: items,
      ),
    );
  }
}

class TitleBlock extends StatelessWidget {
  const TitleBlock(this.title, this.sub, {super.key});
  final String title;
  final String sub;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(20, 24, 20, 14),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(sub, style: const TextStyle(color: Colors.black54)),
        const SizedBox(height: 3),
        Text(
          title,
          style: const TextStyle(
            fontSize: 27,
            color: ink,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    ),
  );
}

class MemberHome extends StatelessWidget {
  const MemberHome(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => ListView(
    children: [
      TitleBlock(
        '\u4f60\u597d\uff0c${session.username}',
        'Star GYM \u4f1a\u5458\u4e2d\u5fc3',
      ),
      Container(
        margin: const EdgeInsets.fromLTRB(20, 0, 20, 22),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: purple,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '\u4eca\u5929\u4e5f\u4e3a\u81ea\u5df1\u52a0\u6cb9',
              style: TextStyle(
                fontSize: 20,
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              '\u5411\u95f8\u673a\u6216\u524d\u53f0\u51fa\u793a\u5165\u573a\u4e8c\u7ef4\u7801',
              style: TextStyle(color: Colors.white70),
            ),
            TextButton.icon(
              onPressed: () => push(context, QrPage(session)),
              icon: const Icon(Icons.qr_code_2, color: Colors.white),
              label: const Text(
                '\u6253\u5f00\u4f1a\u5458\u4e8c\u7ef4\u7801',
                style: TextStyle(color: Colors.white),
              ),
            ),
          ],
        ),
      ),
      ActionGrid(session),
    ],
  );
}

class ActionGrid extends StatelessWidget {
  const ActionGrid(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) {
    final entries = <(String, IconData, Widget)>[
      (
        '\u8d2d\u4e70\u4f1a\u5458\u5361',
        Icons.card_membership_outlined,
        CardsPage(session),
      ),
      ('\u6211\u7684\u4e8c\u7ef4\u7801', Icons.qr_code_2, QrPage(session)),
      (
        '\u8fd0\u52a8\u8bb0\u5f55',
        Icons.edit_note_outlined,
        ExercisePage(session),
      ),
      (
        '\u6708\u5ea6\u7edf\u8ba1',
        Icons.bar_chart_outlined,
        MonthlyPage(session),
      ),
      (
        '\u6211\u7684\u8ba2\u5355',
        Icons.receipt_long_outlined,
        OrdersPage(session),
      ),
      (
        '\u5df2\u7ea6\u8bfe\u7a0b',
        Icons.event_available_outlined,
        BookingsPage(session),
      ),
      (
        '\u9884\u7ea6\u79c1\u6559',
        Icons.person_pin_outlined,
        PrivateBookingPage(session),
      ),
      (
        '\u9000\u5361\u7533\u8bf7',
        Icons.assignment_return_outlined,
        RefundPage(session),
      ),
      (
        '\u901a\u77e5\u4e2d\u5fc3',
        Icons.notifications_none,
        NoticePage(session),
      ),
      (
        '\u6559\u7ec3\u5165\u9a7b',
        Icons.verified_user_outlined,
        CoachApplyPage(session),
      ),
    ];
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: entries.length,
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 4,
          childAspectRatio: .78,
        ),
        itemBuilder: (_, index) {
          final e = entries[index];
          return InkWell(
            onTap: () => push(context, e.$3),
            child: Column(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: index.isEven ? pale : const Color(0xffeaf3ff),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(e.$2, color: index.isEven ? purple : blue),
                ),
                const SizedBox(height: 6),
                Text(
                  e.$1,
                  textAlign: TextAlign.center,
                  style: const TextStyle(fontSize: 11),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

const _qrSecret = 'gym_qr_sign_secret_2026';

String buildQrCode(int userId) {
  final ts = DateTime.now().millisecondsSinceEpoch ~/ 1000;
  final data = '$userId:$ts';
  final hmac = Hmac(sha256, utf8.encode(_qrSecret));
  final sign = hmac.convert(utf8.encode(data)).toString();
  return 'GYM:$userId:$ts:$sign';
}

class QrPage extends StatefulWidget {
  const QrPage(this.session, {super.key});
  final Session session;
  @override
  State<QrPage> createState() => _QrPageState();
}

class _QrPageState extends State<QrPage> {
  late String qrData;
  Timer? timer;
  @override
  void initState() {
    super.initState();
    qrData = buildQrCode(widget.session.id);
    timer = Timer.periodic(const Duration(seconds: 60), (_) {
      if (mounted) setState(() => qrData = buildQrCode(widget.session.id));
    });
  }

  @override
  void dispose() {
    timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u4f1a\u5458\u4e8c\u7ef4\u7801')),
    body: Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            color: Colors.white,
            padding: const EdgeInsets.all(20),
            child: QrImageView(data: qrData, size: 230),
          ),
          const SizedBox(height: 18),
          Text(
            widget.session.username,
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          const Text(
            '\u8bf7\u5411\u95f8\u673a\u6216\u524d\u53f0\u51fa\u793a\u4e8c\u7ef4\u7801',
            style: TextStyle(color: Colors.black54),
          ),
          const SizedBox(height: 4),
          const Text(
            '\u4e8c\u7ef4\u7801\u6709\u6548\u671f 5 \u5206\u949f\uff0c\u81ea\u52a8\u5237\u65b0',
            style: TextStyle(color: Colors.black38, fontSize: 12),
          ),
        ],
      ),
    ),
  );
}

class MonthlyPage extends StatefulWidget {
  const MonthlyPage(this.session, {super.key});
  final Session session;
  @override
  State<MonthlyPage> createState() => _MonthlyPageState();
}

class _MonthlyPageState extends State<MonthlyPage> {
  late Future<Map<String, dynamic>> report;
  @override
  void initState() {
    super.initState();
    report = load();
  }

  Future<Map<String, dynamic>> load() async => Map<String, dynamic>.from(
    await Api.get('/attendance/monthly/${widget.session.username}') as Map,
  );
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u6708\u5ea6\u5230\u5e97\u7edf\u8ba1')),
    body: FutureBuilder<Map<String, dynamic>>(
      future: report,
      builder: (_, snapshot) {
        if (snapshot.hasError) return Center(child: Text('${snapshot.error}'));
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        final d = snapshot.data!;
        final dates = d['activeDayNumbers'] is List
            ? (d['activeDayNumbers'] as List).toSet()
            : <dynamic>{};
        final seconds = (d['totalDurationSeconds'] as num? ?? 0).toInt();
        return ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              '${d['year']}\u5e74${d['month']}\u6708',
              style: const TextStyle(fontSize: 25, fontWeight: FontWeight.bold),
            ),
            const Text(
              '\u6839\u636e\u8fdb\u51fa\u573a\u4e8c\u7ef4\u7801\u8bb0\u5f55\u7edf\u8ba1',
              style: TextStyle(color: Colors.black54),
            ),
            const SizedBox(height: 18),
            Row(
              children: [
                Expanded(
                  child: StatBox(
                    '\u5230\u5e97\u5929\u6570',
                    '${d['activeDays'] ?? 0} \u5929',
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: StatBox(
                    '\u7d2f\u8ba1\u65f6\u957f',
                    '${seconds ~/ 3600}\u5c0f\u65f6${seconds % 3600 ~/ 60}\u5206',
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: List.generate(31, (index) {
                final day = index + 1;
                final hit = dates.contains(day);
                return Container(
                  width: 42,
                  height: 42,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: hit ? purple : const Color(0xfff1f0f5),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    '$day',
                    style: TextStyle(
                      color: hit ? Colors.white : Colors.black54,
                    ),
                  ),
                );
              }),
            ),
          ],
        );
      },
    ),
  );
}

class StatBox extends StatelessWidget {
  const StatBox(this.label, this.value, {super.key});
  final String label;
  final String value;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(14),
    decoration: BoxDecoration(
      color: pale,
      borderRadius: BorderRadius.circular(10),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label),
        const SizedBox(height: 8),
        Text(
          value,
          style: const TextStyle(
            color: purple,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    ),
  );
}

class ExercisePage extends StatefulWidget {
  const ExercisePage(this.session, {super.key});
  final Session session;
  @override
  State<ExercisePage> createState() => _ExercisePageState();
}

class _ExercisePageState extends State<ExercisePage> {
  late Future<List<dynamic>> records;
  @override
  void initState() {
    super.initState();
    records = getList('/api/exercise/${widget.session.username}');
  }

  void reload() {
    final next = getList('/api/exercise/${widget.session.username}');
    if (!mounted) return;
    setState(() {
      records = next;
    });
  }

  Future<void> create() async {
    final saved = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) => ExerciseForm(widget.session),
    );
    if (saved == true) reload();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u8fd0\u52a8\u8bb0\u5f55')),
    floatingActionButton: FloatingActionButton.extended(
      onPressed: create,
      icon: const Icon(Icons.add),
      label: const Text('\u6dfb\u52a0\u8bad\u7ec3'),
    ),
    body: FutureBuilder<List<dynamic>>(
      future: records,
      builder: (_, snapshot) {
        if (snapshot.hasError) return Center(child: Text('${snapshot.error}'));
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        final data = snapshot.data!;
        final minutes = data.fold<int>(
          0,
          (a, x) => a + ((x['durationMinutes'] as num?)?.toInt() ?? 0),
        );
        final sets = data.fold<int>(
          0,
          (a, x) => a + ((x['sets'] as num?)?.toInt() ?? 0),
        );
        final calories = data.fold<double>(
          0,
          (a, x) => a + ((x['calories'] as num?)?.toDouble() ?? 0),
        );
        return ListView(
          padding: const EdgeInsets.all(20),
          children: [
            const Text(
              '\u8bad\u7ec3\u6c47\u603b',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: StatBox(
                    '\u8bad\u7ec3\u65f6\u957f',
                    '$minutes \u5206\u949f',
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: StatBox('\u8bad\u7ec3\u7ec4\u6570', '$sets \u7ec4'),
                ),
              ],
            ),
            const SizedBox(height: 8),
            StatBox(
              '\u8fd0\u52a8\u6d88\u8017',
              '${calories.toStringAsFixed(0)} \u5343\u5361',
            ),
            const SizedBox(height: 18),
            if (data.isEmpty)
              const Padding(
                padding: EdgeInsets.all(30),
                child: Center(
                  child: Text('\u8fd8\u6ca1\u6709\u8bad\u7ec3\u8bb0\u5f55'),
                ),
              )
            else
              ...data.map(
                (x) => Card(
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: pale,
                      child: Text(
                        '${x['bodyPart'] ?? ''}',
                        style: const TextStyle(color: purple),
                      ),
                    ),
                    title: Text('${x['actionName'] ?? ''}'),
                    subtitle: Text(
                      '${x['weight'] ?? 0} \u5343\u514b  ${x['sets'] ?? 0} \u7ec4  ${x['durationMinutes'] ?? 0} \u5206\u949f',
                    ),
                    trailing: Text(
                      '${x['createTime'] ?? ''}'.split('T').first,
                      style: const TextStyle(fontSize: 11),
                    ),
                  ),
                ),
              ),
          ],
        );
      },
    ),
  );
}

class ExerciseForm extends StatefulWidget {
  const ExerciseForm(this.session, {super.key});
  final Session session;
  @override
  State<ExerciseForm> createState() => _ExerciseFormState();
}

class _ExerciseFormState extends State<ExerciseForm> {
  final action = TextEditingController();
  final weight = TextEditingController();
  final sets = TextEditingController();
  final duration = TextEditingController();
  final speed = TextEditingController();
  final incline = TextEditingController();
  String part = '\u80a9';
  String type = '\u529b\u91cf\u8bad\u7ec3';
  bool loading = false;
  @override
  void dispose() {
    action.dispose();
    weight.dispose();
    sets.dispose();
    duration.dispose();
    speed.dispose();
    incline.dispose();
    super.dispose();
  }

  Future<void> save() async {
    if (action.text.trim().isEmpty || int.tryParse(duration.text) == null) {
      showToast(
        context,
        '\u8bf7\u5b8c\u6574\u586b\u5199\u8bad\u7ec3\u4fe1\u606f',
      );
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/api/exercise/${widget.session.username}', {
        'bodyPart': part,
        'exerciseType': type,
        'actionName': action.text.trim(),
        'weight': double.tryParse(weight.text) ?? 0,
        'sets': int.tryParse(sets.text) ?? 0,
        'durationMinutes': int.parse(duration.text),
        'speed': double.tryParse(speed.text),
        'incline': double.tryParse(incline.text),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Padding(
    padding: EdgeInsets.fromLTRB(
      20,
      20,
      20,
      MediaQuery.of(context).viewInsets.bottom + 20,
    ),
    child: ListView(
      shrinkWrap: true,
      children: [
        const Text(
          '\u6dfb\u52a0\u4eca\u65e5\u8bad\u7ec3',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 14),
        DropdownButtonFormField<String>(
          value: type,
          decoration: const InputDecoration(
            labelText: '\u8fd0\u52a8\u5668\u68b0',
            border: OutlineInputBorder(),
          ),
          items: const [
            '\u529b\u91cf\u8bad\u7ec3',
            '\u8dd1\u6b65\u673a',
            '\u722c\u697c\u673a',
            '\u692d\u5706\u4eea',
          ].map((x) => DropdownMenuItem(value: x, child: Text(x))).toList(),
          onChanged: (x) => setState(() => type = x!),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: ['\u80a9', '\u80cc', '\u80f8', '\u81c0', '\u817f']
              .map(
                (x) => ChoiceChip(
                  label: Text(x),
                  selected: part == x,
                  onSelected: (_) => setState(() => part = x),
                ),
              )
              .toList(),
        ),
        const SizedBox(height: 14),
        TextField(
          controller: action,
          decoration: const InputDecoration(
            labelText: '\u8bad\u7ec3\u52a8\u4f5c',
            border: OutlineInputBorder(),
          ),
        ),
        if (type == '\u8dd1\u6b65\u673a' || type == '\u692d\u5706\u4eea')
          TextField(
            controller: speed,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: '\u901f\u5ea6（km/h）',
              border: OutlineInputBorder(),
            ),
          ),
        if (type == '\u8dd1\u6b65\u673a')
          TextField(
            controller: incline,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: '\u5761\u5ea6（%）',
              border: OutlineInputBorder(),
            ),
          ),
        const SizedBox(height: 12),
        TextField(
          controller: weight,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: '\u8bad\u7ec3\u91cd\u91cf\uff08\u5343\u514b\uff09',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: sets,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: '\u8bad\u7ec3\u7ec4\u6570',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: duration,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            labelText: '\u8bad\u7ec3\u65f6\u957f\uff08\u5206\u949f\uff09',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 20),
        FilledButton(
          onPressed: loading ? null : save,
          child: Text(
            loading ? '\u4fdd\u5b58\u4e2d...' : '\u4fdd\u5b58\u8bb0\u5f55',
          ),
        ),
      ],
    ),
  );
}

class CardsPage extends StatelessWidget {
  const CardsPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u8d2d\u4e70\u4f1a\u5458\u5361')),
    body: FutureBuilder<List<dynamic>>(
      future: getList('/api/cards'),
      builder: (_, snapshot) {
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        return ListView(
          padding: const EdgeInsets.all(20),
          children: snapshot.data!.map((x) {
            return Card(
              child: ListTile(
                title: Text('${x['name']}'),
                subtitle: Text('\u6709\u6548\u671f ${x['validDays']} \u5929'),
                trailing: FilledButton(
                  onPressed: () async {
                    try {
                      await Api.post('/api/cards/purchase', {
                        'username': session.username,
                        'cardId': x['id'],
                      });
                      if (context.mounted)
                        showToast(
                          context,
                          '\u8d2d\u4e70\u6210\u529f\uff0c\u5df2\u751f\u6210\u8ba2\u5355',
                        );
                    } catch (e) {
                      if (context.mounted) showToast(context, '$e');
                    }
                  },
                  child: Text('\u00a5${x['price']}'),
                ),
              ),
            );
          }).toList(),
        );
      },
    ),
  );
}

class CoursePage extends StatelessWidget {
  const CoursePage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => FutureBuilder<List<dynamic>>(
    future: getList('/api/courses'),
    builder: (_, snapshot) {
      if (!snapshot.hasData)
        return const Center(child: CircularProgressIndicator());
      return ListView(
        children: [
          const TitleBlock(
            '\u8bfe\u7a0b\u9884\u7ea6',
            '\u56e2\u8bfe / \u79c1\u6559',
          ),
          ...snapshot.data!.map(
            (x) => Card(
              margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
              child: ListTile(
                title: Text('${x['title']}'),
                subtitle: Text(
                  '${x['type']}  ${x['coachName'] ?? ''}\n${x['startTime'] ?? ''}',
                ),
                isThreeLine: true,
                trailing: FilledButton(
                  onPressed: () async {
                    try {
                      await Api.post(
                        '/api/courses/${x['id']}/book/${session.username}',
                        {},
                      );
                      if (context.mounted)
                        showToast(context, '\u9884\u7ea6\u6210\u529f');
                    } catch (e) {
                      if (context.mounted) showToast(context, '$e');
                    }
                  },
                  child: const Text('\u9884\u7ea6'),
                ),
              ),
            ),
          ),
        ],
      );
    },
  );
}

class OrdersPage extends StatelessWidget {
  const OrdersPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => SimpleListPage(
    '\u6211\u7684\u8ba2\u5355',
    '/api/orders/${session.username}',
    (x) => ListTile(
      title: Text('${x['title']}'),
      subtitle: Text('${x['createTime'] ?? ''}'.split('T').first),
      trailing: Text(
        '\u00a5${x['amount']}\n${x['status']}',
        textAlign: TextAlign.end,
      ),
    ),
  );
}

class BookingsPage extends StatelessWidget {
  const BookingsPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => SimpleListPage(
    '\u5df2\u7ea6\u8bfe\u7a0b',
    '/api/bookings/${session.username}',
    (x) => ListTile(
      title: Text('${x['course']?['title'] ?? ''}'),
      subtitle: Text('${x['course']?['startTime'] ?? ''}'),
      trailing: TextButton(
        onPressed: () async {
          await Api.put('/api/bookings/${x['bookingId']}/cancel');
          if (context.mounted)
            showToast(context, '\u5df2\u53d6\u6d88\u9884\u7ea6');
        },
        child: const Text('\u53d6\u6d88'),
      ),
    ),
  );
}

class NoticePage extends StatelessWidget {
  const NoticePage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => SimpleListPage(
    '\u901a\u77e5\u4e2d\u5fc3',
    '/api/notifications/${session.role}?userId=${session.id}',
    (x) => ListTile(
      leading: const Icon(Icons.campaign_outlined, color: purple),
      title: Text('${x['title']}'),
      subtitle: Text('${x['content']}'),
    ),
  );
}

class SimpleListPage extends StatelessWidget {
  const SimpleListPage(this.title, this.path, this.row, {super.key});
  final String title;
  final String path;
  final Widget Function(dynamic) row;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: Text(title)),
    body: FutureBuilder<List<dynamic>>(
      future: getList(path),
      builder: (_, snapshot) {
        if (snapshot.hasError) return Center(child: Text('${snapshot.error}'));
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        if (snapshot.data!.isEmpty)
          return const Center(child: Text('\u6682\u65e0\u6570\u636e'));
        return ListView.separated(
          itemCount: snapshot.data!.length,
          separatorBuilder: (_, __) => const Divider(height: 1),
          itemBuilder: (_, index) => row(snapshot.data![index]),
        );
      },
    ),
  );
}

class RefundPage extends StatefulWidget {
  const RefundPage(this.session, {super.key});
  final Session session;
  @override
  State<RefundPage> createState() => _RefundPageState();
}

class _RefundPageState extends State<RefundPage> {
  late Future<List<dynamic>> cards;
  dynamic selected;
  String? reason;
  final description = TextEditingController();
  bool loading = false;
  final reasons = const [
    '\u8ba1\u5212\u6709\u53d8\u6ca1\u65f6\u95f4\u6d88\u8d39',
    '\u4e70\u591a\u4e86/\u4e70\u9519\u4e86',
    '\u4e0d\u60f3\u8981\u4e86',
    '\u6709\u66f4\u4f18\u60e0\u7684\u6d3b\u52a8',
    '\u5176\u4ed6',
  ];
  @override
  void initState() {
    super.initState();
    cards = getList('/api/cards/owned/${widget.session.username}');
  }

  @override
  void dispose() {
    description.dispose();
    super.dispose();
  }

  Future<void> submit() async {
    if (selected == null || reason == null) {
      showToast(
        context,
        '\u8bf7\u9009\u62e9\u4f1a\u5458\u5361\u548c\u9000\u6b3e\u539f\u56e0',
      );
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/api/refunds', {
        'username': widget.session.username,
        'orderId': selected['id'],
        'reason': reason,
        'description': description.text.trim(),
      });
      if (!mounted) return;
      showToast(
        context,
        '\u9000\u5361\u7533\u8bf7\u5df2\u63d0\u4ea4\uff0c\u7b49\u5f85\u7ba1\u7406\u5458\u5ba1\u6838',
      );
      Navigator.pop(context);
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u9000\u5361\u7533\u8bf7')),
    body: FutureBuilder<List<dynamic>>(
      future: cards,
      builder: (_, snapshot) {
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        if (snapshot.data!.isEmpty)
          return const Center(
            child: Text('\u6682\u65e0\u53ef\u9000\u7684\u4f1a\u5458\u5361'),
          );
        return ListView(
          padding: const EdgeInsets.all(20),
          children: [
            const Text(
              '\u9009\u62e9\u8981\u9000\u7684\u4f1a\u5458\u5361',
              style: TextStyle(fontSize: 19, fontWeight: FontWeight.bold),
            ),
            ...snapshot.data!.map(
              (x) => Card(
                child: RadioListTile<dynamic>(
                  value: x,
                  groupValue: selected,
                  onChanged: (value) => setState(() => selected = value),
                  title: Text('${x['title']}'),
                  subtitle: Text('\u8ba2\u5355\u7f16\u53f7\uff1a${x['id']}'),
                  secondary: Text(
                    '\u00a5${x['amount']}',
                    style: const TextStyle(
                      color: purple,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ),
            if (selected != null) ...[
              const SizedBox(height: 16),
              StatBox(
                '\u9000\u6b3e\u91d1\u989d',
                '\u00a5${selected['amount']}',
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: reason,
                decoration: const InputDecoration(
                  labelText: '\u9000\u6b3e\u539f\u56e0\uff08\u5fc5\u9009\uff09',
                  border: OutlineInputBorder(),
                ),
                items: reasons
                    .map((x) => DropdownMenuItem(value: x, child: Text(x)))
                    .toList(),
                onChanged: (value) => setState(() => reason = value),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: description,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: '\u9000\u6b3e\u8bf4\u660e\uff08\u9009\u586b\uff09',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 20),
              SizedBox(
                height: 50,
                child: FilledButton(
                  onPressed: loading ? null : submit,
                  child: Text(
                    loading
                        ? '\u63d0\u4ea4\u4e2d...'
                        : '\u63d0\u4ea4\u9000\u5361\u7533\u8bf7',
                  ),
                ),
              ),
            ],
          ],
        );
      },
    ),
  );
}

class ProfilePage extends StatelessWidget {
  const ProfilePage(this.session, {super.key});
  final Session session;
  Future<void> logout(BuildContext context) async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('\u9000\u51fa\u767b\u5f55'),
        content: const Text(
          '\u786e\u5b9a\u8981\u9000\u51fa\u5f53\u524d\u8d26\u53f7\u5417\uff1f',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('\u53d6\u6d88'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('\u9000\u51fa'),
          ),
        ],
      ),
    );
    if (yes == true && context.mounted)
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (_) => const WelcomePage()),
        (_) => false,
      );
  }

  @override
  Widget build(BuildContext context) => ListView(
    children: [
      TitleBlock(session.username, '\u6211\u7684'),
      ListTile(
        leading: const Icon(Icons.qr_code_2),
        title: const Text('\u4f1a\u5458\u4e8c\u7ef4\u7801'),
        onTap: () => push(context, QrPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.edit_note_outlined),
        title: const Text('\u8fd0\u52a8\u8bb0\u5f55'),
        onTap: () => push(context, ExercisePage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.bar_chart_outlined),
        title: const Text('\u6708\u5ea6\u7edf\u8ba1'),
        onTap: () => push(context, MonthlyPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.receipt_long),
        title: const Text('\u6211\u7684\u8ba2\u5355'),
        onTap: () => push(context, OrdersPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.event_available),
        title: const Text('\u5df2\u7ea6\u8bfe\u7a0b'),
        onTap: () => push(context, BookingsPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.person_pin_outlined),
        title: const Text('\u9884\u7ea6\u79c1\u6559'),
        onTap: () => push(context, PrivateBookingPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.assignment_return_outlined),
        title: const Text('\u9000\u5361\u7533\u8bf7'),
        onTap: () => push(context, RefundPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.notifications_none),
        title: const Text('\u901a\u77e5\u4e2d\u5fc3'),
        onTap: () => push(context, NoticePage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.card_membership_outlined),
        title: const Text('\u4f1a\u5458\u6743\u76ca'),
        onTap: () => push(context, MembershipPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.lock_outline),
        title: const Text('\u4fee\u6539\u5bc6\u7801'),
        onTap: () => push(context, PasswordPage(session)),
      ),
      const Divider(),
      ListTile(
        leading: const Icon(Icons.logout, color: Colors.red),
        title: const Text(
          '\u9000\u51fa\u767b\u5f55',
          style: TextStyle(color: Colors.red),
        ),
        onTap: () => logout(context),
      ),
    ],
  );
}

class CoachHome extends StatelessWidget {
  const CoachHome(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => ListView(
    children: [
      TitleBlock('\u6559\u7ec3\u5de5\u4f5c\u53f0', session.username),
      ListTile(
        leading: const Icon(Icons.groups_outlined, color: purple),
        title: const Text('\u5e26\u56e2\u8bfe'),
        onTap: () => push(context, CoursePage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.person_pin_outlined, color: purple),
        title: const Text('\u79c1\u6559\u8bfe\u7a0b'),
        onTap: () => push(context, AvailabilityPage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.checklist_outlined, color: purple),
        title: const Text('\u65e5\u7a0b\u8868 / \u6e05\u5355'),
        onTap: () => push(context, CoursePage(session)),
      ),
      ListTile(
        leading: const Icon(Icons.logout, color: Colors.red),
        title: const Text(
          '\u9000\u51fa\u767b\u5f55',
          style: TextStyle(color: Colors.red),
        ),
        onTap: () => Navigator.pushAndRemoveUntil(
          context,
          MaterialPageRoute(builder: (_) => const WelcomePage()),
          (_) => false,
        ),
      ),
    ],
  );
}

class MembershipPage extends StatelessWidget {
  const MembershipPage(this.session, {super.key});
  final Session session;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u4f1a\u5458\u6743\u76ca')),
    body: FutureBuilder<List<dynamic>>(
      future: getList('/api/memberships/${session.username}'),
      builder: (_, snapshot) {
        if (snapshot.hasError) return Center(child: Text('${snapshot.error}'));
        if (!snapshot.hasData)
          return const Center(child: CircularProgressIndicator());
        if (snapshot.data!.isEmpty)
          return const Center(child: Text('\u6682\u65e0\u6709\u6548\u4f1a\u5458\u5361\uff0c\u8bf7\u5148\u8d2d\u4e70'));
        return ListView.separated(
          padding: const EdgeInsets.all(20),
          itemCount: snapshot.data!.length,
          separatorBuilder: (_, __) => const SizedBox(height: 10),
          itemBuilder: (_, index) {
            final x = snapshot.data![index];
            final active = '${x['status'] ?? ''}' == 'ACTIVE';
            return Card(
              child: ListTile(
                leading: Icon(
                  active ? Icons.workspace_premium : Icons.card_giftcard,
                  color: active ? purple : Colors.grey,
                ),
                title: Text('${x['cardName'] ?? ''}'),
                subtitle: Text(
                  '\u751f\u6548\uff1a${'${x['startDate']}'.split('T').first}\n\u5230\u671f\uff1a${'${x['endDate']}'.split('T').first}',
                ),
                isThreeLine: true,
                trailing: Text(
                  '${x['status']}',
                  style: TextStyle(
                    color: active ? purple : Colors.black54,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            );
          },
        );
      },
    ),
  );
}

class PasswordPage extends StatefulWidget {
  const PasswordPage(this.session, {super.key});
  final Session session;
  @override
  State<PasswordPage> createState() => _PasswordPageState();
}

class _PasswordPageState extends State<PasswordPage> {
  final oldPassword = TextEditingController();
  final newPassword = TextEditingController();
  bool loading = false;
  @override
  void dispose() {
    oldPassword.dispose();
    newPassword.dispose();
    super.dispose();
  }

  Future<void> save() async {
    if (oldPassword.text.isEmpty || newPassword.text.length < 6) {
      showToast(context, '\u8bf7\u8f93\u5165\u65e7\u5bc6\u7801\uff0c\u4e14\u65b0\u5bc6\u7801\u81f3\u5c11 6 \u4f4d');
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/user/password/change', {
        'username': widget.session.username,
        'oldPassword': oldPassword.text,
        'newPassword': newPassword.text,
      });
      if (!mounted) return;
      showToast(context, '\u5bc6\u7801\u4fee\u6539\u6210\u529f');
      Navigator.pop(context);
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('\u4fee\u6539\u5bc6\u7801')),
    body: Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          TextField(
            controller: oldPassword,
            obscureText: true,
            decoration: const InputDecoration(
              labelText: '\u65e7\u5bc6\u7801',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: newPassword,
            obscureText: true,
            decoration: const InputDecoration(
              labelText: '\u65b0\u5bc6\u7801\uff08\u81f3\u5c11 6 \u4f4d\uff09',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            height: 50,
            child: FilledButton(
              onPressed: loading ? null : save,
              child: Text(loading ? '\u63d0\u4ea4\u4e2d...' : '\u786e\u8ba4\u4fee\u6539'),
            ),
          ),
        ],
      ),
    ),
  );
}
