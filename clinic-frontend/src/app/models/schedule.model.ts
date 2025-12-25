export interface ScheduleResponse {
  scheduleId: number;
  doctorId?: number;
  doctorName?: string;
  receptionistId?: number;
  receptionistName?: string;
  specialtyId?: number;
  specialty?: string;
  workDate: string;
  startTime: string;
  endTime: string;
}

export interface ScheduleCreationRequest {
  doctorId: number;
  workDate: string;
  startTime: string;
  endTime: string;
}

export interface ScheduleUpdationRequest {
  workDate: string;
  startTime: string;
  endTime: string;
}
