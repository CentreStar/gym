import 'package:flutter/material.dart';
import 'main.dart';

class CoachApplyPage extends StatefulWidget {
  const CoachApplyPage(this.session, {super.key});
  final Session session;
  @override
  State<CoachApplyPage> createState() => _CoachApplyPageState();
}

class _CoachApplyPageState extends State<CoachApplyPage> {
  final name = TextEditingController();
  final phone = TextEditingController();
  final proof = TextEditingController();
  final intro = TextEditingController();
  bool loading = false;
  @override
  void dispose() {
    name.dispose();
    phone.dispose();
    proof.dispose();
    intro.dispose();
    super.dispose();
  }

  Future<void> submit() async {
    if (name.text.trim().isEmpty || proof.text.trim().isEmpty) {
      showToast(context, '请填写姓名和资质证明');
      return;
    }
    setState(() => loading = true);
    try {
      await Api.post('/coach/apply', {
        'userId': widget.session.id,
        'name': name.text.trim(),
        'phone': phone.text.trim(),
        'description': intro.text.trim(),
        'proofMaterial': proof.text.trim(),
      });
      if (mounted) {
        showToast(context, '申请已提交，等待管理员审核');
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) showToast(context, '$e');
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('教练入驻')),
    body: ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const Text(
          '提交资料后，管理员审核通过才会切换为教练账号。',
          style: TextStyle(color: Colors.black54),
        ),
        const SizedBox(height: 16),
        TextField(
          controller: name,
          decoration: const InputDecoration(
            labelText: '姓名',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: phone,
          decoration: const InputDecoration(
            labelText: '联系电话',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: proof,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: '资质证明（必填）',
            hintText: '例如：国家职业资格证书编号、培训证书说明',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: intro,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: '个人介绍（选填）',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 20),
        FilledButton(
          onPressed: loading ? null : submit,
          child: Text(loading ? '提交中...' : '提交申请'),
        ),
      ],
    ),
  );
}
