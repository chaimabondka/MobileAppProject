import 'dart:convert';
import 'dart:typed_data';

import 'package:http/http.dart' as http;

import '../manager/imgbb_config.dart';

/// Service to upload images to ImgBB (free, no credit card required).
class ImgBbService {
  static const _uploadUrl = 'https://api.imgbb.com/1/upload';

  /// Upload image bytes to ImgBB and return the public URL.
  /// [imageBytes] - raw image bytes (PNG, JPEG, etc.)
  static Future<String> uploadImage(Uint8List imageBytes) async {
    if (imgBbApiKey.isEmpty || imgBbApiKey == 'YOUR_IMGBB_API_KEY') {
      throw Exception(
        'ImgBB API key not configured. Get a free key at https://api.imgbb.com/ '
        'and add it in lib/manager/imgbb_config.dart',
      );
    }

    final base64Image = base64Encode(imageBytes);

    final response = await http.post(
      Uri.parse(_uploadUrl),
      body: {
        'key': imgBbApiKey,
        'image': base64Image,
      },
    );

    if (response.statusCode != 200) {
      throw Exception(
        'ImgBB upload failed: ${response.statusCode} - ${response.body}',
      );
    }

    final json = jsonDecode(response.body) as Map<String, dynamic>;

    if (json['success'] != true) {
      throw Exception(
        'ImgBB upload failed: ${json['error']?['message'] ?? response.body}',
      );
    }

    final data = json['data'] as Map<String, dynamic>;
    final url = data['url'] as String?;

    if (url == null || url.isEmpty) {
      throw Exception('ImgBB did not return image URL');
    }

    return url;
  }
}
