import { formatDate } from '@angular/common';
export class AdvanceTable {
  id: number;
  img: string;
  firstName: string;
  lastName: string;
  email: string;
  gender: string;
  birthDate: string;
  mobile: string;
  address: string;
  country: string;
  constructor(advanceTable: Partial<AdvanceTable> = {}) {
    {
      this.id = advanceTable.id || this.getRandomID();
      this.img = advanceTable.img || 'assets/images/user/user1.jpg';
      this.firstName = advanceTable.firstName || '';
      this.lastName = advanceTable.lastName || '';
      this.email = advanceTable.email || '';
      this.gender = advanceTable.gender || 'male';
      this.birthDate = formatDate(new Date(), 'yyyy-MM-dd', 'en') || '';
      this.mobile = advanceTable.mobile || '';
      this.address = advanceTable.address || '';
      this.country = advanceTable.country || '';
    }
  }
  public getRandomID(): number {
    const S4 = () => {
      return ((1 + Math.random()) * 0x10000) | 0;
    };
    return S4() + S4();
  }
}
