import { Component, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../../api/chatbot.service';

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
export class ChatbotComponent { // BỎ AfterViewChecked ở đây
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  private chatbotService = inject(ChatbotService);

  isOpen = false;
  isLoading = false;
  userQuestion = '';
  unreadCount = 0;

  messages: ChatMessage[] = [
    { text: 'Xin chào! Tôi là trợ lý ảo AI của 28Care. Bạn cần hỗ trợ gì?', sender: 'bot' }
  ];

  ngOnInit() {
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
      // Cuộn xuống nhẹ nhàng khi vừa mở cửa sổ
      setTimeout(() => this.scrollToBottom(), 50);
    }
  }

  sendMessage() {
    if (!this.userQuestion.trim() || this.isLoading) return;

    const question = this.userQuestion;
    this.messages.push({ text: question, sender: 'user' });
    this.userQuestion = '';
    this.isLoading = true;

    // 3. SỬA LỖI TRÔI: Chỉ cuộn xuống ngay lúc bấm Send (để thấy câu hỏi và bong bóng loading)
    setTimeout(() => this.scrollToBottom(), 50);

    this.chatbotService.sendMessage(question).subscribe({
      next: (res) => {
        const botReply = res.result ? res.result.answer : "Tôi không hiểu câu hỏi.";
        this.messages.push({ text: botReply, sender: 'bot' });
        this.isLoading = false;
        // LƯU Ý KỸ: Ở đây KHÔNG gọi scrollToBottom() nữa.
        // Khi đoạn text dài hiện ra, màn hình sẽ "đứng im" ngay đầu câu hỏi của user.
      },
      error: (err) => {
        console.error(err);
        this.messages.push({ text: 'Hệ thống đang bảo trì, vui lòng thử lại sau.', sender: 'bot' });
        this.isLoading = false;
      }
    });
  }

  private scrollToBottom(): void {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    }
  }
}
