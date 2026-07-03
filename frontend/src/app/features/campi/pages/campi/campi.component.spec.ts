import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampiComponent } from './campi.component';

describe('CampiComponent', () => {
  let component: CampiComponent;
  let fixture: ComponentFixture<CampiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CampiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
