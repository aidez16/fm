import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { StatCardComponent } from './stat-card.component';

describe('StatCardComponent', () => {
  let fixture: ComponentFixture<StatCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatCardComponent);
    fixture.componentRef.setInput('icon', 'groups');
    fixture.componentRef.setInput('label', 'Client Accounts');
    fixture.componentRef.setInput('value', '4');
  });

  it('renders the provided label and value', () => {
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Client Accounts');
    expect(text).toContain('4');
  });

  it('defaults to neutral tone (no positive/negative class)', () => {
    fixture.detectChanges();
    const card = fixture.debugElement.query(By.css('.stat-card'));
    expect(card.classes['stat-card--positive']).toBeFalsy();
    expect(card.classes['stat-card--negative']).toBeFalsy();
  });

  it('applies the positive tone class when tone is positive', () => {
    fixture.componentRef.setInput('tone', 'positive');
    fixture.detectChanges();
    const card = fixture.debugElement.query(By.css('.stat-card'));
    expect(card.classes['stat-card--positive']).toBeTrue();
  });

  it('applies the negative tone class when tone is negative', () => {
    fixture.componentRef.setInput('tone', 'negative');
    fixture.detectChanges();
    const card = fixture.debugElement.query(By.css('.stat-card'));
    expect(card.classes['stat-card--negative']).toBeTrue();
  });
});
