import { Component, ElementRef, ViewChild, AfterViewChecked, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../../api/chatbot.service'; // Import Service vừa tạo

interface ChatMessage {
  text: string;
  sender: 'user' | 'bot';
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule], // Import FormsModule để dùng ngModel
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  // Inject Service
  private chatbotService = inject(ChatbotService);

  isOpen = false;
  isLoading = false;
  userQuestion = '';
  unreadCount = 0;

  messages: ChatMessage[] = [
    { text: 'Xin chào! Tôi là trợ lý ảo AI của 28Care. Bạn cần hỗ trợ gì?', sender: 'bot' }
  ];

  ngOnInit() {
    // Giả lập: Sau 1 giây load trang thì báo có tin nhắn mới
    setTimeout(() => {
      if (!this.isOpen) { // Chỉ hiện nếu chat đang đóng
        this.unreadCount = 1;
      }
    }, 1000);
  }

  toggleChat() {
    this.isOpen = !this.isOpen;

    // 2. KHI MỞ CHAT -> RESET ĐẾM VỀ 0
    if (this.isOpen) {
      this.unreadCount = 0;
    }
  }

  sendMessage() {
    if (!this.userQuestion.trim() || this.isLoading) return;

    const question = this.userQuestion;

    // 1. Hiển thị tin nhắn User
    this.messages.push({ text: question, sender: 'user' });
    this.userQuestion = '';
    this.isLoading = true;

    // 2. Gọi Service
    this.chatbotService.sendMessage(question).subscribe({
      next: (res) => {
        // Map dữ liệu từ Backend trả về (Sửa 'result.answer' tùy theo DTO của bạn)
        const botReply = res.result ? res.result.answer : "Tôi không hiểu câu hỏi.";
        this.messages.push({ text: botReply, sender: 'bot' });
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.messages.push({ text: 'Hệ thống đang bảo trì, vui lòng thử lại sau.', sender: 'bot' });
        this.isLoading = false;
      }
    });
  }

  // Tự động cuộn xuống cuối khi có tin nhắn mới
  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }
}
