import os
import requests
import json

BASE_URL = "https://integrate.api.nvidia.com/v1/chat/completions"


def _get_headers():
    api_key = os.getenv("NVIDIA_NIM_API_KEY")
    if not api_key:
        raise RuntimeError(
            "NVIDIA_NIM_API_KEY environment variable is missing from the sandbox context."
        )
    return {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }


def list_project_files():
    ignored_dirs = {'.git', '__pycache__', 'node_modules', '.idea', 'build', '.gradle', 'app/build'}
    files_list = []
    for root, dirs, files in os.walk('.'):
        dirs[:] = [d for d in dirs if d not in ignored_dirs]
        for file in files:
            if file.endswith(('.kt', '.java', '.py', '.json', '.md', '.gradle', '.xml')):
                files_list.append(os.path.join(root, file))
    return files_list

print("🤖 مستعد لمساعدتك في فحص وتعديل مستودع Nabd-Agent-OS عبر خوادم NVIDIA مباشرة!")
print("-------------------------------------------------------------------")

# الضمير : التأكد من وجود المفتاح قبل بدء الجلسة
try:
    _get_headers()
    print("✅ NVIDIA_NIM_API_KEY detected")
except RuntimeError as e:
    print(f"❌ {e}")
    exit(1)

while True:
    user_input = input("\n👤 عمار: ")
    if user_input.lower() == 'exit':
        break

    files = list_project_files()
    context = "Here are the core project files for your context:\n"
    for f in files[:5]:
        try:
            with open(f, 'r', encoding='utf-8') as file_content:
                context += f"\n--- File: {f} ---\n{file_content.read()}\n"
        except:
            pass

    payload = {
        "model": "meta/llama-3.3-70b-instruct",
        "messages": [
            {"role": "system", "content": f"You are an expert software architect and mobile developer. Here is the context of the user's repository:\n{context}\n\nProvide deep code review, architectural insights, and structure analysis based on the files provided."},
            {"role": "user", "content": user_input}
        ],
        "temperature": 0.2,
        "max_tokens": 4096
    }

    try:
        response = requests.post(BASE_URL, headers=_get_headers(), json=payload)
        if response.status_code == 200:
            result = response.json()
            reply = result['choices'][0]['message']['content']
            print(f"\n🤖 المساعد:\n{reply}")
        else:
            print(f"\n❌ خطأ في الاتصال بخوادم انفيديا: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"\n❌ تعذر الاتصال بالشبكة: {e}")
