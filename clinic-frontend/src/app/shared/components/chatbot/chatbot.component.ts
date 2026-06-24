import { Component, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../../api/chatbot.service';
import { AuthService } from '../../../api/auth.service';

interface ChatMessage {
  text: string;
  sender: 'user' | 'bot';
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  private chatbotService = inject(ChatbotService);
  private authService = inject(AuthService);

  isOpen = false;
  isLoading = false;
  userQuestion = '';
  unreadCount = 0;
  private patientId: number | null = null;

  messages: ChatMessage[] = [
    { text: 'Xin chào! Tôi là trợ lý ảo AI của 28Care. Bạn cần hỗ trợ gì?', sender: 'bot' }
  ];

  ngOnInit() {
    // Lấy patientId khi user đã đăng nhập
    this.authService.currentPatientId$.subscribe(id => {
      this.patientId = id;
    });

    setTimeout(() => {
      if (!this.isOpen) {
        this.unreadCount = 1;
      }
    }, 1000);
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.unreadCount = 0;
      setTimeout(() => this.scrollToBottom(), 50);
    }
  }

  sendMessage() {
    if (!this.userQuestion.trim() || this.isLoading) return;

    const question = this.userQuestion;
    this.messages.push({ text: question, sender: 'user' });
    this.userQuestion = '';
    this.isLoading = true;

    // Vẫn cuộn xuống đáy khi user vừa gửi tin nhắn (để thấy hiệu ứng loading)
    setTimeout(() => this.scrollToBottom(), 50);

    this.chatbotService.sendMessage(question, this.patientId).subscribe({
      next: (res) => {
        const botReply = res.result ? res.result.answer : "Tôi không hiểu câu hỏi.";
        this.messages.push({ text: botReply, sender: 'bot' });
        this.isLoading = false;

        // Gọi hàm cuộn đến câu hỏi của user sau khi bot trả lời
        setTimeout(() => this.scrollToLastUserMessage(), 50);
      },
      error: (err) => {
        console.error(err);
        this.messages.push({ text: 'Hệ thống đang bảo trì, vui lòng thử lại sau.', sender: 'bot' });
        this.isLoading = false;

        // Cả khi lỗi cũng cuộn lên cho đồng bộ trải nghiệm
        setTimeout(() => this.scrollToLastUserMessage(), 50);
      }
    });
  }

  private scrollToBottom(): void {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    }
  }

  // THÊM HÀM MỚI: Định vị và cuộn đến câu hỏi cuối cùng của user
  private scrollToLastUserMessage(): void {
    if (!this.scrollContainer) return;

    const container = this.scrollContainer.nativeElement;
    // Tìm tất cả các tin nhắn được gửi từ user trong DOM
    const userMessages = container.querySelectorAll('.message-wrapper.user');

    if (userMessages.length > 0) {
      // Lấy phần tử câu hỏi cuối cùng (chính là câu vừa hỏi)
      const lastUserMessage = userMessages[userMessages.length - 1];

      // Cuộn mượt mà sao cho khối tin nhắn này nằm sát mép trên (start) của container
      lastUserMessage.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}
